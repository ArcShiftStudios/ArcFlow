package com.example.data.repository

import com.example.data.database.ArcCalendarDatabase
import com.example.data.entity.CalendarEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.EventEntity
import com.example.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class CalendarRepository(private val database: ArcCalendarDatabase) {
    val allEvents: Flow<List<EventEntity>> = database.eventDao().getAllEvents()
    val allTasks: Flow<List<TaskEntity>> = database.taskDao().getAllTasks()
    val allCategories: Flow<List<CategoryEntity>> = database.categoryDao().getAllCategories()
    val allCalendars: Flow<List<CalendarEntity>> = database.calendarDao().getAllCalendars()
    val pinnedEvents: Flow<List<EventEntity>> = database.eventDao().getPinnedEvents()

    fun getEventsForDate(epochDay: Long): Flow<List<EventEntity>> =
        database.eventDao().getEventsForDate(epochDay)

    fun getEventsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<EventEntity>> =
        database.eventDao().getEventsBetween(startEpochDay, endEpochDay)

    suspend fun getEventById(id: Long): EventEntity? =
        database.eventDao().getEventById(id)

    suspend fun insertEvent(event: EventEntity): Long =
        database.eventDao().insertEvent(event)

    suspend fun updateEvent(event: EventEntity) =
        database.eventDao().updateEvent(event)

    suspend fun deleteEvent(event: EventEntity) =
        database.eventDao().deleteEvent(event)

    suspend fun deleteEventById(id: Long) =
        database.eventDao().deleteEventById(id)

    fun searchEvents(query: String): Flow<List<EventEntity>> =
        database.eventDao().searchEvents(query)

    fun getTasksForDate(epochDay: Long): Flow<List<TaskEntity>> =
        database.taskDao().getTasksForDate(epochDay)

    suspend fun getTaskById(id: Long): TaskEntity? =
        database.taskDao().getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long =
        database.taskDao().insertTask(task)

    suspend fun updateTask(task: TaskEntity) =
        database.taskDao().updateTask(task)

    suspend fun deleteTask(task: TaskEntity) =
        database.taskDao().deleteTask(task)

    suspend fun deleteTaskById(id: Long) =
        database.taskDao().deleteTaskById(id)

    fun searchTasks(query: String): Flow<List<TaskEntity>> =
        database.taskDao().searchTasks(query)

    suspend fun insertCategory(category: CategoryEntity) =
        database.categoryDao().insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) =
        database.categoryDao().updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        database.categoryDao().deleteCategory(category)

    suspend fun insertCalendar(calendar: CalendarEntity): Long =
        database.calendarDao().insertCalendar(calendar)

    suspend fun updateCalendar(calendar: CalendarEntity) =
        database.calendarDao().updateCalendar(calendar)

    suspend fun deleteCalendar(calendar: CalendarEntity) =
        database.calendarDao().deleteCalendar(calendar)
}
