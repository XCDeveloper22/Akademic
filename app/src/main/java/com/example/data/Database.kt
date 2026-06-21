package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "semesters")
data class Semester(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = Semester::class,
            parentColumns = ["id"],
            childColumns = ["semesterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("semesterId")]
)
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val semesterId: Int,
    val code: String,
    val name: String,
    val credits: Double,
    val gradeString: String, // "A+", "A", "B", etc.
    val gradePoints: Double // 4.0, 3.0, etc.
)

@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val code: String,
    val dayOfWeek: String, // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    val startTime: String, // "09:00" (24h format for sorting)
    val endTime: String, // "10:30" (24h format)
    val room: String,
    val colorHex: String // "#9A162B" etc.
)

@Dao
interface AcademicDao {
    // Semesters
    @Query("SELECT * FROM semesters ORDER BY id ASC")
    fun getAllSemesters(): Flow<List<Semester>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: Semester): Long

    @Delete
    suspend fun deleteSemester(semester: Semester)

    @Query("DELETE FROM semesters WHERE id = :id")
    suspend fun deleteSemesterById(id: Int)

    // Courses
    @Query("SELECT * FROM courses WHERE semesterId = :semesterId ORDER BY id ASC")
    fun getCoursesForSemester(semesterId: Int): Flow<List<Course>>

    @Query("SELECT * FROM courses ORDER BY id ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: Int)

    // Schedule Items
    @Query("SELECT * FROM schedule_items ORDER BY startTime ASC")
    fun getAllScheduleItems(): Flow<List<ScheduleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleItem)

    @Delete
    suspend fun deleteScheduleItem(item: ScheduleItem)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteScheduleItemById(id: Int)
}

@Database(entities = [Semester::class, Course::class, ScheduleItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun academicDao(): AcademicDao
}
