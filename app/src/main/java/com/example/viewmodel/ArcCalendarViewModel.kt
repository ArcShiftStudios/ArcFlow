package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ArcCalendarDatabase
import com.example.data.entity.CalendarEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.EventEntity
import com.example.data.entity.TaskEntity
import com.example.data.repository.CalendarRepository
import com.example.service.EventParser
import com.example.service.IcsHelper
import com.example.service.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

enum class NavigationDestination {
    TODAY, CALENDAR, TASKS, STATS, SETTINGS
}

enum class CalendarViewMode {
    PLANNER, GRID, TIMELINE, WEEK, MONTH
}

data class FocusSession(
    val eventId: Long? = null,
    val title: String = "",
    val categoryId: String = "study",
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
)

class ArcCalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ArcCalendarDatabase.getDatabase(application)
    private val repository = CalendarRepository(database)
    private val prefs = application.getSharedPreferences("arc_calendar_prefs", Context.MODE_PRIVATE)

    // Navigation & View states
    private val _currentDestination = MutableStateFlow(NavigationDestination.TODAY)
    val currentDestination: StateFlow<NavigationDestination> = _currentDestination.asStateFlow()

    private val _calendarViewMode = MutableStateFlow(CalendarViewMode.PLANNER)
    val calendarViewMode: StateFlow<CalendarViewMode> = _calendarViewMode.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Themes & Appearance
    private val _themePresetId = MutableStateFlow(prefs.getString("theme_preset", "arc_blue") ?: "arc_blue")
    val themePresetId: StateFlow<String> = _themePresetId.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "DARK") ?: "DARK")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicDailyAccents = MutableStateFlow(prefs.getBoolean("dynamic_accents", true))
    val dynamicDailyAccents: StateFlow<Boolean> = _dynamicDailyAccents.asStateFlow()

    private val _gestureParticles = MutableStateFlow(prefs.getBoolean("gesture_particles", true))
    val gestureParticles: StateFlow<Boolean> = _gestureParticles.asStateFlow()

    private val _ambientDots = MutableStateFlow(prefs.getBoolean("ambient_dots", true))
    val ambientDots: StateFlow<Boolean> = _ambientDots.asStateFlow()

    private val _customPrimaryHex = MutableStateFlow<String?>(prefs.getString("custom_primary_hex", null))
    val customPrimaryHex: StateFlow<String?> = _customPrimaryHex.asStateFlow()

    // Preferences
    private val _firstDayOfWeek = MutableStateFlow(prefs.getString("first_day_of_week", "MONDAY") ?: "MONDAY")
    val firstDayOfWeek: StateFlow<String> = _firstDayOfWeek.asStateFlow()

    private val _is24HourFormat = MutableStateFlow(prefs.getBoolean("is_24_hour", true))
    val is24HourFormat: StateFlow<Boolean> = _is24HourFormat.asStateFlow()

    // Data streams
    val allEvents: StateFlow<List<EventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCalendars: StateFlow<List<CalendarEntity>> = repository.allCalendars
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedEvents: StateFlow<List<EventEntity>> = repository.pinnedEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Events for Selected Date
    val selectedDateEvents: StateFlow<List<EventEntity>> = combine(allEvents, _selectedDate) { events, date ->
        val epochDay = date.toEpochDay()
        events.filter { event ->
            if (event.dateEpochDay == epochDay) true
            else when (event.recurrence) {
                "DAILY" -> date.toEpochDay() >= event.dateEpochDay
                "WEEKDAYS" -> date.toEpochDay() >= event.dateEpochDay && date.dayOfWeek.value in 1..5
                "WEEKLY" -> date.toEpochDay() >= event.dateEpochDay && LocalDate.ofEpochDay(event.dateEpochDay).dayOfWeek == date.dayOfWeek
                "MONTHLY" -> date.toEpochDay() >= event.dateEpochDay && LocalDate.ofEpochDay(event.dateEpochDay).dayOfMonth == date.dayOfMonth
                else -> false
            }
        }.sortedBy { it.startMinuteOfDay }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Up Next Event (Hero event for Today)
    val upNextEvent: StateFlow<EventEntity?> = combine(allEvents, _selectedDate) { events, _ ->
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()
        val currentMinute = LocalTime.now().hour * 60 + LocalTime.now().minute
        val todayList = events.filter { it.dateEpochDay == todayEpoch && !it.isCompleted }
        todayList.firstOrNull { it.endMinuteOfDay >= currentMinute }
            ?: todayList.firstOrNull()
            ?: events.firstOrNull { it.dateEpochDay > todayEpoch && !it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI Sheets and Modals
    private val _viewingEvent = MutableStateFlow<EventEntity?>(null)
    val viewingEvent: StateFlow<EventEntity?> = _viewingEvent.asStateFlow()

    private val _isAddEventOpen = MutableStateFlow(false)
    val isAddEventOpen: StateFlow<Boolean> = _isAddEventOpen.asStateFlow()

    private val _editingEvent = MutableStateFlow<EventEntity?>(null)
    val editingEvent: StateFlow<EventEntity?> = _editingEvent.asStateFlow()

    private val _isAddTaskOpen = MutableStateFlow(false)
    val isAddTaskOpen: StateFlow<Boolean> = _isAddTaskOpen.asStateFlow()

    private val _editingTask = MutableStateFlow<TaskEntity?>(null)
    val editingTask: StateFlow<TaskEntity?> = _editingTask.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    // Focus Mode
    private val _focusSession = MutableStateFlow<FocusSession?>(null)
    val focusSession: StateFlow<FocusSession?> = _focusSession.asStateFlow()

    // Category / Calendar Manage Dialogs
    private val _isManageCategoriesOpen = MutableStateFlow(false)
    val isManageCategoriesOpen: StateFlow<Boolean> = _isManageCategoriesOpen.asStateFlow()

    private val _isThemesOpen = MutableStateFlow(false)
    val isThemesOpen: StateFlow<Boolean> = _isThemesOpen.asStateFlow()

    init {
        NotificationHelper.createNotificationChannel(application)
    }

    fun navigateTo(dest: NavigationDestination) {
        _currentDestination.value = dest
    }

    fun setCalendarViewMode(mode: CalendarViewMode) {
        _calendarViewMode.value = mode
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun jumpToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun nextMonth() {
        _selectedDate.value = _selectedDate.value.plusMonths(1)
    }

    fun prevMonth() {
        _selectedDate.value = _selectedDate.value.minusMonths(1)
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun prevDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun setThemePreset(id: String) {
        _themePresetId.value = id
        _customPrimaryHex.value = null
        prefs.edit().putString("theme_preset", id).remove("custom_primary_hex").apply()
    }

    fun setCustomTheme(primaryHex: String) {
        _customPrimaryHex.value = primaryHex
        _themePresetId.value = "custom"
        prefs.edit().putString("theme_preset", "custom").putString("custom_primary_hex", primaryHex).apply()
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun toggleDynamicAccents() {
        val newVal = !_dynamicDailyAccents.value
        _dynamicDailyAccents.value = newVal
        prefs.edit().putBoolean("dynamic_accents", newVal).apply()
    }

    fun toggleGestureParticles() {
        val newVal = !_gestureParticles.value
        _gestureParticles.value = newVal
        prefs.edit().putBoolean("gesture_particles", newVal).apply()
    }

    fun toggleAmbientDots() {
        val newVal = !_ambientDots.value
        _ambientDots.value = newVal
        prefs.edit().putBoolean("ambient_dots", newVal).apply()
    }

    fun setFirstDayOfWeek(day: String) {
        _firstDayOfWeek.value = day
        prefs.edit().putString("first_day_of_week", day).apply()
    }

    fun toggleTimeFormat() {
        val newVal = !_is24HourFormat.value
        _is24HourFormat.value = newVal
        prefs.edit().putBoolean("is_24_hour", newVal).apply()
    }

    // Event operations
    fun openAddEvent(date: LocalDate = _selectedDate.value) {
        _selectedDate.value = date
        _editingEvent.value = null
        _isAddEventOpen.value = true
    }

    fun openEditEvent(event: EventEntity) {
        _editingEvent.value = event
        _isAddEventOpen.value = true
    }

    fun closeAddEvent() {
        _isAddEventOpen.value = false
        _editingEvent.value = null
    }

    fun viewEvent(event: EventEntity) {
        _viewingEvent.value = event
    }

    fun closeEventDetails() {
        _viewingEvent.value = null
    }

    fun saveEvent(
        title: String,
        description: String,
        categoryId: String,
        calendarId: Long,
        dateEpochDay: Long,
        startMinute: Int,
        endMinute: Int,
        isPinned: Boolean,
        recurrence: String,
        reminderMinutes: Int,
        location: String,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = _editingEvent.value
            val entity = if (existing != null) {
                existing.copy(
                    title = title,
                    description = description,
                    categoryId = categoryId,
                    calendarId = calendarId,
                    dateEpochDay = dateEpochDay,
                    startMinuteOfDay = startMinute,
                    endMinuteOfDay = endMinute,
                    isPinned = isPinned,
                    recurrence = recurrence,
                    reminderMinutesBefore = reminderMinutes,
                    location = location,
                    notes = notes
                )
            } else {
                EventEntity(
                    title = title,
                    description = description,
                    categoryId = categoryId,
                    calendarId = calendarId,
                    dateEpochDay = dateEpochDay,
                    startMinuteOfDay = startMinute,
                    endMinuteOfDay = endMinute,
                    isPinned = isPinned,
                    recurrence = recurrence,
                    reminderMinutesBefore = reminderMinutes,
                    location = location,
                    notes = notes
                )
            }

            if (existing != null) {
                repository.updateEvent(entity)
                NotificationHelper.scheduleEventReminder(getApplication(), entity)
            } else {
                val newId = repository.insertEvent(entity)
                NotificationHelper.scheduleEventReminder(getApplication(), entity.copy(id = newId))
            }
            _viewingEvent.value = null
            _isAddEventOpen.value = false
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            NotificationHelper.cancelEventReminder(getApplication(), event.id)
            repository.deleteEvent(event)
            if (_viewingEvent.value?.id == event.id) {
                _viewingEvent.value = null
            }
        }
    }

    fun toggleEventPin(event: EventEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEvent(event.copy(isPinned = !event.isPinned))
            if (_viewingEvent.value?.id == event.id) {
                _viewingEvent.value = _viewingEvent.value?.copy(isPinned = !event.isPinned)
            }
        }
    }

    fun toggleEventCompleted(event: EventEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEvent(event.copy(isCompleted = !event.isCompleted))
        }
    }

    // Task operations
    fun openAddTask(date: LocalDate? = _selectedDate.value) {
        _editingTask.value = null
        _isAddTaskOpen.value = true
    }

    fun openEditTask(task: TaskEntity) {
        _editingTask.value = task
        _isAddTaskOpen.value = true
    }

    fun closeAddTask() {
        _isAddTaskOpen.value = false
        _editingTask.value = null
    }

    fun saveTask(
        title: String,
        description: String,
        categoryId: String,
        dueDateEpochDay: Long?,
        dueTimeMinute: Int?,
        priority: Int,
        isPinned: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = _editingTask.value
            val entity = if (existing != null) {
                existing.copy(
                    title = title,
                    description = description,
                    categoryId = categoryId,
                    dueDateEpochDay = dueDateEpochDay,
                    dueTimeMinute = dueTimeMinute,
                    priority = priority,
                    isPinned = isPinned
                )
            } else {
                TaskEntity(
                    title = title,
                    description = description,
                    categoryId = categoryId,
                    dueDateEpochDay = dueDateEpochDay,
                    dueTimeMinute = dueTimeMinute,
                    priority = priority,
                    isPinned = isPinned
                )
            }

            if (existing != null) {
                repository.updateTask(entity)
            } else {
                repository.insertTask(entity)
            }
            _isAddTaskOpen.value = false
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val isNowCompleted = !task.isCompleted
            val completedTime = if (isNowCompleted) System.currentTimeMillis() else null
            repository.updateTask(task.copy(isCompleted = isNowCompleted, completedAt = completedTime))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(task)
        }
    }

    // Focus Mode
    fun startFocusSession(event: EventEntity? = null, customMinutes: Int = 25) {
        val durationSeconds = if (event != null) {
            ((event.endMinuteOfDay - event.startMinuteOfDay) * 60).coerceAtLeast(15 * 60)
        } else {
            customMinutes * 60
        }
        _focusSession.value = FocusSession(
            eventId = event?.id,
            title = event?.title ?: "Deep Focus",
            categoryId = event?.categoryId ?: "study",
            totalSeconds = durationSeconds,
            remainingSeconds = durationSeconds,
            isRunning = true,
            isPaused = false
        )
    }

    fun updateFocusTick() {
        val current = _focusSession.value ?: return
        if (current.isRunning && !current.isPaused && current.remainingSeconds > 0) {
            _focusSession.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
        }
    }

    fun togglePauseFocus() {
        val current = _focusSession.value ?: return
        _focusSession.value = current.copy(isPaused = !current.isPaused)
    }

    fun completeFocusSession() {
        val current = _focusSession.value ?: return
        if (current.eventId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val ev = repository.getEventById(current.eventId)
                if (ev != null) repository.updateEvent(ev.copy(isCompleted = true))
            }
        }
        _focusSession.value = null
    }

    fun exitFocusSession() {
        _focusSession.value = null
    }

    // Search
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openSearch() {
        _isSearchOpen.value = true
    }

    fun closeSearch() {
        _isSearchOpen.value = false
        _searchQuery.value = ""
    }

    // Categories and Calendars
    fun openManageCategories() {
        _isManageCategoriesOpen.value = true
    }

    fun closeManageCategories() {
        _isManageCategoriesOpen.value = false
    }

    fun openThemes() {
        _isThemesOpen.value = true
    }

    fun closeThemes() {
        _isThemesOpen.value = false
    }

    fun saveCategory(id: String, name: String, colorHex: String, iconName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCategory(CategoryEntity(id, name, colorHex, iconName))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(category)
        }
    }

    fun toggleCalendarVisibility(calendar: CalendarEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCalendar(calendar.copy(isVisible = !calendar.isVisible))
        }
    }

    // ICS / Backup export and import
    fun exportIcs(): String {
        return IcsHelper.exportToIcs(allEvents.value)
    }

    fun importIcs(icsText: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val parsed = IcsHelper.parseIcs(icsText)
            for (ev in parsed) {
                repository.insertEvent(ev)
            }
            onComplete(parsed.size)
        }
    }
}
