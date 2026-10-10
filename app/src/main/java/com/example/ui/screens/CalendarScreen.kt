package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.EventEntity
import com.example.ui.components.EventCard
import com.example.ui.theme.LocalArcTheme
import com.example.ui.theme.getCategoryColor
import com.example.viewmodel.ArcCalendarViewModel
import com.example.viewmodel.CalendarViewMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: ArcCalendarViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    val isDark = theme.isDark
    val primaryColor = theme.preset.primary

    val selectedDate by viewModel.selectedDate.collectAsState()
    val viewMode by viewModel.calendarViewMode.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val selectedEvents by viewModel.selectedDateEvents.collectAsState()
    val is24Hour by viewModel.is24HourFormat.collectAsState()

    val yearMonth = remember(selectedDate) { YearMonth.of(selectedDate.year, selectedDate.month) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
    ) {
        // Top Month Navigation Row + Jump to Today
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.prevMonth() },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF161926) else Color(0xFFEDF2F7))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()} ${yearMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { viewModel.nextMonth() },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF161926) else Color(0xFFEDF2F7))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Today jump pill with professional gradient
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                primaryColor.copy(alpha = if (isDark) 0.28f else 0.18f),
                                theme.preset.accentGradientEnd.copy(alpha = if (isDark) 0.18f else 0.12f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.5f),
                                theme.preset.accentGradientEnd.copy(alpha = 0.3f)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { viewModel.jumpToToday() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("jump_today_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TODAY",
                    color = primaryColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // View Mode Selector Pills (PLANNER, GRID, TIMELINE, WEEK, MONTH)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CalendarViewMode.values()) { mode ->
                val isSelected = viewMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(
                                    listOf(
                                        primaryColor,
                                        theme.preset.accentGradientEnd
                                    )
                                )
                            } else if (isDark) {
                                Brush.linearGradient(listOf(Color(0xFF141724), Color(0xFF141724)))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFFEEF2F6), Color(0xFFEEF2F6)))
                            }
                        )
                        .clickable { viewModel.setCalendarViewMode(mode) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("view_mode_${mode.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dynamic View Rendering based on ViewMode
        AnimatedContent(
            targetState = viewMode,
            transitionSpec = {
                fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
            },
            label = "calendar_view_transition",
            modifier = Modifier.weight(1f)
        ) { mode ->
            when (mode) {
                CalendarViewMode.PLANNER -> PlannerView(
                    selectedDate = selectedDate,
                    onSelectDate = { viewModel.setSelectedDate(it) },
                    events = allEvents,
                    is24Hour = is24Hour,
                    onEventClick = { viewModel.viewEvent(it) },
                    onFocusClick = { viewModel.startFocusSession(it) },
                    onAddEvent = { viewModel.openAddEvent(it) }
                )
                CalendarViewMode.GRID, CalendarViewMode.MONTH -> MonthGridView(
                    yearMonth = yearMonth,
                    selectedDate = selectedDate,
                    onSelectDate = { viewModel.setSelectedDate(it) },
                    events = allEvents,
                    selectedDateEvents = selectedEvents,
                    is24Hour = is24Hour,
                    onEventClick = { viewModel.viewEvent(it) },
                    onFocusClick = { viewModel.startFocusSession(it) }
                )
                CalendarViewMode.TIMELINE -> DayTimelineView(
                    selectedDate = selectedDate,
                    events = selectedEvents,
                    is24Hour = is24Hour,
                    onSelectDate = { viewModel.setSelectedDate(it) },
                    onEventClick = { viewModel.viewEvent(it) },
                    onFocusClick = { viewModel.startFocusSession(it) }
                )
                CalendarViewMode.WEEK -> WeekScheduleView(
                    selectedDate = selectedDate,
                    onSelectDate = { viewModel.setSelectedDate(it) },
                    allEvents = allEvents,
                    is24Hour = is24Hour,
                    onEventClick = { viewModel.viewEvent(it) },
                    onFocusClick = { viewModel.startFocusSession(it) }
                )
            }
        }
    }
}

@Composable
fun PlannerView(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    events: List<EventEntity>,
    is24Hour: Boolean,
    onEventClick: (EventEntity) -> Unit,
    onFocusClick: (EventEntity) -> Unit,
    onAddEvent: (LocalDate) -> Unit
) {
    val theme = LocalArcTheme.current
    val isDark = theme.isDark
    val primaryColor = theme.preset.primary

    // 14 days preview starting from selectedDate minus 2 days
    val startDate = remember(selectedDate) { selectedDate.minusDays(2) }
    val days = remember(startDate) { (0..14).map { startDate.plusDays(it.toLong()) } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(days) { date ->
            val isCurrentSelected = date == selectedDate
            val isRealToday = date == LocalDate.now()
            val dayEvents = events.filter { ev ->
                ev.dateEpochDay == date.toEpochDay() || when (ev.recurrence) {
                    "DAILY" -> date.toEpochDay() >= ev.dateEpochDay
                    "WEEKDAYS" -> date.toEpochDay() >= ev.dateEpochDay && date.dayOfWeek.value in 1..5
                    "WEEKLY" -> date.toEpochDay() >= ev.dateEpochDay && LocalDate.ofEpochDay(ev.dateEpochDay).dayOfWeek == date.dayOfWeek
                    "MONTHLY" -> date.toEpochDay() >= ev.dateEpochDay && LocalDate.ofEpochDay(ev.dateEpochDay).dayOfMonth == date.dayOfMonth
                    else -> false
                }
            }.sortedBy { it.startMinuteOfDay }

            val containerBg = if (isDark) {
                if (isCurrentSelected) Color(0xFF171B2B) else Color(0xFF10121D)
            } else {
                if (isCurrentSelected) Color(0xFFEBF3FF) else Color(0xFFFFFFFF)
            }

            val containerBorder = if (isCurrentSelected) {
                primaryColor.copy(alpha = 0.55f)
            } else {
                if (isDark) Color(0xFF1E2436) else Color(0xFFE2E8F0)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(containerBg)
                    .border(1.dp, containerBorder, RoundedCornerShape(26.dp))
                    .clickable { onSelectDate(date) }
                    .padding(18.dp)
            ) {
                // Header of Day Card: SATURDAY 03 / Day Progress / + button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isRealToday) primaryColor else if (isDark) Color(0xFF22283D) else Color(0xFFE2E8F0))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = String.format("%02d", date.dayOfMonth),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isRealToday) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column {
                            Text(
                                text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isRealToday) primaryColor else MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${date.year}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { onAddEvent(date) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF22283D) else Color(0xFFE2E8F0))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Event",
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (dayEvents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dayEvents.forEach { ev ->
                            EventCard(
                                event = ev,
                                isHero = false,
                                is24Hour = is24Hour,
                                onClick = { onEventClick(ev) },
                                onFocusClick = { onFocusClick(ev) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthGridView(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    events: List<EventEntity>,
    selectedDateEvents: List<EventEntity>,
    is24Hour: Boolean,
    onEventClick: (EventEntity) -> Unit,
    onFocusClick: (EventEntity) -> Unit
) {
    val theme = LocalArcTheme.current
    val isDark = theme.isDark
    val primaryColor = theme.preset.primary

    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Monday, 7 = Sunday
    val daysInMonth = yearMonth.lengthOfMonth()

    val totalCells = ((firstDayOfWeek - 1) + daysInMonth + 6) / 7 * 7

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Calendar Grid Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (isDark) Color(0xFF10121C) else Color(0xFFFFFFFF))
                    .border(1.dp, if (isDark) Color(0xFF1E2436) else Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                    .padding(18.dp)
            ) {
                // Day of Week Headers (M T W T F S S)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLetter ->
                        Text(
                            text = dayLetter,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days Grid
                val weeksCount = totalCells / 7
                for (w in 0 until weeksCount) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (d in 1..7) {
                            val cellIndex = w * 7 + d
                            val dayNum = cellIndex - (firstDayOfWeek - 1)
                            if (dayNum in 1..daysInMonth) {
                                val date = yearMonth.atDay(dayNum)
                                val isSelected = date == selectedDate
                                val isToday = date == LocalDate.now()
                                val hasEvents = events.any { it.dateEpochDay == date.toEpochDay() }

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) primaryColor
                                            else if (isToday) primaryColor.copy(alpha = 0.20f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            width = if (isToday && !isSelected) 1.dp else 0.dp,
                                            color = if (isToday && !isSelected) primaryColor else Color.Transparent,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onSelectDate(date) }
                                        .testTag("calendar_day_${date.dayOfMonth}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNum",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White
                                            else if (isToday) primaryColor
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (hasEvents) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else primaryColor)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Agenda Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()} — ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${selectedDateEvents.size} events",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )
            }
        }

        if (selectedDateEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color(0xFF10121C) else Color(0xFFF8FAFC))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No events scheduled for this day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(selectedDateEvents) { ev ->
                EventCard(
                    event = ev,
                    isHero = false,
                    is24Hour = is24Hour,
                    onClick = { onEventClick(ev) },
                    onFocusClick = { onFocusClick(ev) }
                )
            }
        }
    }
}

@Composable
fun DayTimelineView(
    selectedDate: LocalDate,
    events: List<EventEntity>,
    is24Hour: Boolean,
    onSelectDate: (LocalDate) -> Unit,
    onEventClick: (EventEntity) -> Unit,
    onFocusClick: (EventEntity) -> Unit
) {
    val theme = LocalArcTheme.current
    val isDark = theme.isDark
    val primaryColor = theme.preset.primary

    val currentNow = LocalTime.now()
    val isSelectedToday = selectedDate == LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp)
    ) {
        // Date Header
        Text(
            text = "${selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()} • ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Hours from 07:00 to 23:00
        for (hour in 7..23) {
            val hourEvents = events.filter {
                val evStartH = it.startMinuteOfDay / 60
                evStartH == hour
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Time Label
                Text(
                    text = if (is24Hour) String.format("%02d:00", hour)
                    else String.format("%d %s", if (hour % 12 == 0) 12 else hour % 12, if (hour >= 12) "PM" else "AM"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(54.dp)
                )

                // Timeline Line and Content
                Column(modifier = Modifier.weight(1f)) {
                    // Current time line if today and current hour
                    if (isSelectedToday && currentNow.hour == hour) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Divider(color = Color(0xFFEF4444), thickness = 2.dp)
                        }
                    }

                    if (hourEvents.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            hourEvents.forEach { ev ->
                                EventCard(
                                    event = ev,
                                    isHero = false,
                                    is24Hour = is24Hour,
                                    onClick = { onEventClick(ev) },
                                    onFocusClick = { onFocusClick(ev) }
                                )
                            }
                        }
                    } else {
                        Divider(
                            color = if (isDark) Color(0xFF191D2C) else Color(0xFFE2E8F0),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeekScheduleView(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    allEvents: List<EventEntity>,
    is24Hour: Boolean,
    onEventClick: (EventEntity) -> Unit,
    onFocusClick: (EventEntity) -> Unit
) {
    val theme = LocalArcTheme.current
    val isDark = theme.isDark
    val primaryColor = theme.preset.primary

    val monday = remember(selectedDate) { selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val weekDays = remember(monday) { (0..6).map { monday.plusDays(it.toLong()) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Week Days Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { date ->
                val isSelected = date == selectedDate
                val isToday = date == LocalDate.now()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) primaryColor
                            else if (isToday) primaryColor.copy(alpha = 0.20f)
                            else if (isDark) Color(0xFF141724)
                            else Color(0xFFF1F5F9)
                        )
                        .clickable { onSelectDate(date) }
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${date.dayOfMonth}",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Day Events in Week
        val dayEvents = allEvents.filter { it.dateEpochDay == selectedDate.toEpochDay() }.sortedBy { it.startMinuteOfDay }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "${selectedDate.dayOfWeek.name} SCHEDULE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    letterSpacing = 1.sp
                )
            }

            if (dayEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) Color(0xFF10121C) else Color(0xFFF8FAFC))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No events on this day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(dayEvents) { ev ->
                    EventCard(
                        event = ev,
                        isHero = false,
                        is24Hour = is24Hour,
                        onClick = { onEventClick(ev) },
                        onFocusClick = { onFocusClick(ev) }
                    )
                }
            }
        }
    }
}
