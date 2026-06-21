package com.example.data

import kotlinx.coroutines.flow.Flow

class AcademicRepository(private val dao: AcademicDao) {
    // Semesters
    val allSemesters: Flow<List<Semester>> = dao.getAllSemesters()

    suspend fun insertSemester(semester: Semester): Long {
        return dao.insertSemester(semester)
    }

    suspend fun deleteSemester(semester: Semester) {
        dao.deleteSemester(semester)
    }

    suspend fun deleteSemesterById(id: Int) {
        dao.deleteSemesterById(id)
    }

    // Courses
    val allCourses: Flow<List<Course>> = dao.getAllCourses()

    fun getCoursesForSemester(semesterId: Int): Flow<List<Course>> {
        return dao.getCoursesForSemester(semesterId)
    }

    suspend fun insertCourse(course: Course) {
        dao.insertCourse(course)
    }

    suspend fun deleteCourse(course: Course) {
        dao.deleteCourse(course)
    }

    suspend fun deleteCourseById(id: Int) {
        dao.deleteCourseById(id)
    }

    // Schedule
    val allScheduleItems: Flow<List<ScheduleItem>> = dao.getAllScheduleItems()

    suspend fun insertSchedule(item: ScheduleItem) {
        dao.insertScheduleItem(item)
    }

    suspend fun deleteSchedule(item: ScheduleItem) {
        dao.deleteScheduleItem(item)
    }

    suspend fun deleteScheduleById(id: Int) {
        dao.deleteScheduleItemById(id)
    }

    // Tasks
    val allTasks: Flow<List<Task>> = dao.getAllTasks()

    suspend fun insertTask(task: Task): Long {
        return dao.insertTask(task)
    }

    suspend fun deleteTask(task: Task) {
        dao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Int) {
        dao.deleteTaskById(id)
    }
}
