package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "akademic_db"
    ).build()

    private val repository = AcademicRepository(db.academicDao())

    // App state
    val currentScreen = MutableStateFlow("SPLASH")
    val selectedSemesterId = MutableStateFlow<Int?>(null)

    // Data streams
    val semesters: StateFlow<List<Semester>> = repository.allSemesters
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val courses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val scheduleItems: StateFlow<List<ScheduleItem>> = repository.allScheduleItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Collect semesters to set the initial selected semester ID when the list is populated by the user
        viewModelScope.launch {
            semesters.collect { list ->
                if (selectedSemesterId.value == null && list.isNotEmpty()) {
                    selectedSemesterId.value = list.firstOrNull()?.id
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        // Seed Semesters
        val sem1Id = repository.insertSemester(Semester(name = "1st Year - 1st Semester")).toInt()
        val sem2Id = repository.insertSemester(Semester(name = "1st Year - 2nd Semester")).toInt()

        // Seed Courses following standard MSU scale (1.00 to 5.00)
        // 1.00 is highest, 3.00 is passing, 5.00 is fail.
        repository.insertCourse(Course(semesterId = sem1Id, code = "COSC 101", name = "Intro to Computer Science", credits = 3.0, gradeString = "1.25", gradePoints = 1.25))
        repository.insertCourse(Course(semesterId = sem1Id, code = "MATH 201", name = "Calculus I", credits = 4.0, gradeString = "1.50", gradePoints = 1.50))
        repository.insertCourse(Course(semesterId = sem1Id, code = "ENGL 110", name = "Expository Writing", credits = 3.0, gradeString = "1.00", gradePoints = 1.00))

        repository.insertCourse(Course(semesterId = sem2Id, code = "COSC 102", name = "Object-Oriented Programming", credits = 3.0, gradeString = "1.00", gradePoints = 1.00))
        repository.insertCourse(Course(semesterId = sem2Id, code = "MATH 202", name = "Discrete Structures", credits = 3.0, gradeString = "2.00", gradePoints = 2.00))
        repository.insertCourse(Course(semesterId = sem2Id, code = "PE 101", name = "Physical Education", credits = 2.0, gradeString = "INC", gradePoints = -1.0))

        // Seed Schedule Items
        repository.insertSchedule(ScheduleItem(title = "Database Systems", code = "COSC 301", dayOfWeek = "Monday", startTime = "09:00", endTime = "10:30", room = "Lab 3B", colorHex = "#9B1B1B"))
        repository.insertSchedule(ScheduleItem(title = "Calculus Workshop", code = "MATH 201", dayOfWeek = "Monday", startTime = "11:00", endTime = "12:30", room = "Rm 402", colorHex = "#D4AF37"))
        repository.insertSchedule(ScheduleItem(title = "Software Engineering", code = "COSC 310", dayOfWeek = "Tuesday", startTime = "13:30", endTime = "15:00", room = "Rm 101", colorHex = "#9B1B1B"))
        repository.insertSchedule(ScheduleItem(title = "Object-Oriented Programming", code = "COSC 102", dayOfWeek = "Wednesday", startTime = "09:00", endTime = "10:30", room = "Lab 1A", colorHex = "#D4AF37"))
    }

    // Semesters Actions
    fun addSemester(name: String) {
        viewModelScope.launch {
            val id = repository.insertSemester(Semester(name = name))
            if (selectedSemesterId.value == null) {
                selectedSemesterId.value = id.toInt()
            }
        }
    }

    fun deleteSemester(semesterId: Int) {
        viewModelScope.launch {
            repository.deleteSemesterById(semesterId)
            if (selectedSemesterId.value == semesterId) {
                // Pick another semester
                val other = semesters.value.firstOrNull { it.id != semesterId }
                selectedSemesterId.value = other?.id
            }
        }
    }

    fun selectSemester(semesterId: Int) {
        selectedSemesterId.value = semesterId
    }

    // Course Actions
    fun addCourse(semesterId: Int, code: String, name: String, credits: Double, gradeString: String) {
        val gradePoints = getPointsForGrade(gradeString)
        viewModelScope.launch {
            repository.insertCourse(
                Course(
                    semesterId = semesterId,
                    code = code,
                    name = name,
                    credits = credits,
                    gradeString = gradeString,
                    gradePoints = gradePoints
                )
            )
        }
    }

    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            repository.deleteCourseById(courseId)
        }
    }

    // Schedule Actions
    fun addScheduleItem(title: String, code: String, dayOfWeek: String, startTime: String, endTime: String, room: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertSchedule(
                ScheduleItem(
                    title = title,
                    code = code,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    room = room,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteScheduleItem(itemId: Int) {
        viewModelScope.launch {
            repository.deleteScheduleById(itemId)
        }
    }

    // GWA / GPA scale helper based on Mindanao State University Scale
    // 1.00 is highest/Excellent, 3.00 is passing, 5.00 is failure.
    // Non-numeric grades return -1.0 to flag exclusion from GWA calculation.
    fun getPointsForGrade(grade: String): Double {
        return when (grade.uppercase().trim()) {
            "1.00" -> 1.00
            "1.25" -> 1.25
            "1.50" -> 1.50
            "1.75" -> 1.75
            "2.00" -> 2.00
            "2.25" -> 2.25
            "2.50" -> 2.50
            "2.75" -> 2.75
            "3.00" -> 3.00
            "5.00" -> 5.00
            else -> -1.0 // "INC", "IP", "DRP", "ODP", "W", "WP" are non-GPA affecting
        }
    }

    val gradeScaleOptions = listOf(
        "1.00", "1.25", "1.50", "1.75", "2.00", "2.25", "2.50", "2.75", "3.00", "5.00",
        "INC", "IP", "DRP", "ODP", "W", "WP"
    )
}
