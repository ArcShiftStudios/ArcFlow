package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CalendarDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.EventDao
import com.example.data.dao.TaskDao
import com.example.data.entity.CalendarEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.EventEntity
import com.example.data.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [EventEntity::class, TaskEntity::class, CategoryEntity::class, CalendarEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ArcCalendarDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun calendarDao(): CalendarDao

    companion object {
        @Volatile
        private var INSTANCE: ArcCalendarDatabase? = null

        fun getDatabase(context: Context): ArcCalendarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArcCalendarDatabase::class.java,
                    "arccalendar_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: ArcCalendarDatabase) {
                val categoryDao = db.categoryDao()
                val calendarDao = db.calendarDao()
                val eventDao = db.eventDao()
                val taskDao = db.taskDao()

                // Default categories matching ArcShift specification
                val defaultCategories = listOf(
                    CategoryEntity("study", "Study", "#3B82F6", "school", true),
                    CategoryEntity("projects", "Projects", "#10B981", "code", true),
                    CategoryEntity("personal", "Personal", "#8B5CF6", "person", true),
                    CategoryEntity("important", "Important", "#F59E0B", "star", true),
                    CategoryEntity("deadline", "Deadline", "#EF4444", "alarm", true),
                    CategoryEntity("health", "Health", "#06B6D4", "favorite", true),
                    CategoryEntity("neutral", "Neutral", "#64748B", "coffee", true)
                )
                categoryDao.insertCategories(defaultCategories)

                // Default Calendars
                val defaultCalendars = listOf(
                    CalendarEntity(1, "Personal", "#8B5CF6", true, true),
                    CalendarEntity(2, "School / Work", "#3B82F6", true, false),
                    CalendarEntity(3, "Arc Projects", "#10B981", true, false)
                )
                calendarDao.insertCalendars(defaultCalendars)

                // Populate only two clean initial events
                val today = LocalDate.now()
                val todayEpoch = today.toEpochDay()

                val initialEvents = listOf(
                    EventEntity(
                        id = 1,
                        title = "Physics",
                        description = "Mirrors & Ray Optics Revision",
                        categoryId = "study",
                        calendarId = 2,
                        dateEpochDay = todayEpoch,
                        startMinuteOfDay = 15 * 60, // 15:00
                        endMinuteOfDay = 16 * 60 + 30, // 16:30
                        isPinned = true,
                        reminderMinutesBefore = 15,
                        location = "Classroom 3B",
                        notes = "Chapter 1 - 3 problem set review",
                        iconName = "school"
                    ),
                    EventEntity(
                        id = 2,
                        title = "Tuition",
                        description = "Mathematics - Advanced Calculus",
                        categoryId = "projects",
                        calendarId = 2,
                        dateEpochDay = todayEpoch,
                        startMinuteOfDay = 17 * 60, // 17:00
                        endMinuteOfDay = 18 * 60, // 18:00
                        isPinned = false,
                        reminderMinutesBefore = 10,
                        location = "Arc Learning Hub",
                        notes = "Bring Graphing calculator and past papers",
                        iconName = "menu_book"
                    )
                )

                for (ev in initialEvents) {
                    eventDao.insertEvent(ev)
                }

                // Populate only two clean initial tasks
                val initialTasks = listOf(
                    TaskEntity(
                        id = 1,
                        title = "Complete Maths worksheet",
                        description = "Questions 1-20 on Differential equations",
                        categoryId = "study",
                        dueDateEpochDay = todayEpoch,
                        dueTimeMinute = 16 * 60,
                        priority = 2,
                        isPinned = true
                    ),
                    TaskEntity(
                        id = 2,
                        title = "Finish ArcCalendar UI design",
                        description = "Verify contrast and particle canvas overlay",
                        categoryId = "projects",
                        dueDateEpochDay = todayEpoch,
                        dueTimeMinute = 20 * 60,
                        priority = 3,
                        isPinned = true
                    )
                )

                for (task in initialTasks) {
                    taskDao.insertTask(task)
                }
            }
        }
    }
}
