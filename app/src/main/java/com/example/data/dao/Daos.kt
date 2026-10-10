package com.example.data.dao

import androidx.room.*
import com.example.data.entity.CalendarEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.EventEntity
import com.example.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY dateEpochDay ASC, startMinuteOfDay ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE dateEpochDay = :epochDay ORDER BY startMinuteOfDay ASC")
    fun getEventsForDate(epochDay: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay ASC, startMinuteOfDay ASC")
    fun getEventsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE isPinned = 1 ORDER BY dateEpochDay ASC, startMinuteOfDay ASC")
    fun getPinnedEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("SELECT * FROM events WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%'")
    fun searchEvents(query: String): Flow<List<EventEntity>>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, dueDateEpochDay ASC, id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDateEpochDay = :epochDay ORDER BY isCompleted ASC, priority DESC")
    fun getTasksForDate(epochDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchTasks(query: String): Flow<List<TaskEntity>>
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendars")
    fun getAllCalendars(): Flow<List<CalendarEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendar(calendar: CalendarEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCalendars(calendars: List<CalendarEntity>)

    @Update
    suspend fun updateCalendar(calendar: CalendarEntity)

    @Delete
    suspend fun deleteCalendar(calendar: CalendarEntity)
}
