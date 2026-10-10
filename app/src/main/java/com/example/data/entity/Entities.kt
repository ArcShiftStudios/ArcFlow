package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryId: String = "study",
    val calendarId: Long = 1,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val startMinuteOfDay: Int, // e.g. 15:00 -> 900
    val endMinuteOfDay: Int, // e.g. 16:30 -> 990
    val isAllDay: Boolean = false,
    val isPinned: Boolean = false,
    val isCompleted: Boolean = false,
    val recurrence: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY, WEEKDAYS
    val reminderMinutesBefore: Int = 15, // -1 for none, 0 for at time, 15, 30, etc.
    val location: String = "",
    val notes: String = "",
    val colorHex: String? = null,
    val iconName: String = "event",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryId: String = "study",
    val dueDateEpochDay: Long? = null,
    val dueTimeMinute: Int? = null,
    val isCompleted: Boolean = false,
    val priority: Int = 1, // 0 = low, 1 = medium, 2 = high, 3 = urgent
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "calendars")
data class CalendarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val isVisible: Boolean = true,
    val isDefault: Boolean = false
)
