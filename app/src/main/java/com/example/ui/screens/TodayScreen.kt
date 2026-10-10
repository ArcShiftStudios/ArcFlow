package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.EventEntity
import com.example.data.entity.TaskEntity
import com.example.ui.components.EventCard
import com.example.ui.components.TaskCard
import com.example.ui.theme.LocalArcTheme
import com.example.viewmodel.ArcCalendarViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TodayScreen(
    viewModel: ArcCalendarViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    val isDark = theme.isDark
    val primaryColor = theme.preset.primary

    val todayEvents by viewModel.selectedDateEvents.collectAsState()
    val pinnedEvents by viewModel.pinnedEvents.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val upNextEvent by viewModel.upNextEvent.collectAsState()
    val is24Hour by viewModel.is24HourFormat.collectAsState()

    val today = LocalDate.now()
    val weekdayStr = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase(Locale.getDefault())
    val dateNumStr = String.format("%02d.%02d", today.dayOfMonth, today.monthValue)
    val monthStr = today.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase(Locale.getDefault())

    // Calculate Day Progress percentage
    val now = LocalTime.now()
    val currentMinute = now.hour * 60 + now.minute
    val dayProgressPercent = (currentMinute.toFloat() / 1440f * 100f).toInt().coerceIn(0, 100)

    val pendingTasks = allTasks.filter { !it.isCompleted }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("today_screen_scroll"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero Editorial Date Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                primaryColor.copy(alpha = if (isDark) 0.22f else 0.14f),
                                theme.preset.accentGradientEnd.copy(alpha = if (isDark) 0.14f else 0.08f),
                                if (isDark) Color(0xFF10121B) else Color(0xFFF8FAFC)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                primaryColor.copy(alpha = if (isDark) 0.40f else 0.25f),
                                theme.preset.accentGradientEnd.copy(alpha = if (isDark) 0.25f else 0.15f)
                            )
                        ),
                        RoundedCornerShape(32.dp)
                    )
                    .padding(24.dp)
            ) {
                // Top line: Weekday & Weather/Location Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = weekdayStr,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        letterSpacing = 1.5.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0x33FFFFFF) else Color(0x15000000))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WbSunny,
                            contentDescription = "Weather",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "24°C • Focus Day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Big Editorial Date: 03.10
                Text(
                    text = dateNumStr,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-2).sp
                )

                // Month Name: OCTOBER
                Text(
                    text = monthStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Day Progress Bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (is24Hour) String.format("%02d:%02d", now.hour, now.minute)
                            else String.format("%d:%02d %s", if (now.hour % 12 == 0) 12 else now.hour % 12, now.minute, if (now.hour >= 12) "PM" else "AM"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = "$dayProgressPercent% of day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { dayProgressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = primaryColor,
                        trackColor = if (isDark) Color(0xFF1E2436) else Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // UP NEXT / Primary Focus Event
        if (upNextEvent != null) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UP NEXT",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Primary Focus",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    EventCard(
                        event = upNextEvent!!,
                        isHero = true,
                        is24Hour = is24Hour,
                        onClick = { viewModel.viewEvent(upNextEvent!!) },
                        onFocusClick = { viewModel.startFocusSession(upNextEvent!!) }
                    )
                }
            }
        }

        // Pinned Events Section
        val uncompletedPinned = pinnedEvents.filter { !it.isCompleted && it.id != upNextEvent?.id }
        if (uncompletedPinned.isNotEmpty()) {
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "PINNED EVENTS",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uncompletedPinned.take(3).forEach { event ->
                            EventCard(
                                event = event,
                                isHero = false,
                                is24Hour = is24Hour,
                                onClick = { viewModel.viewEvent(event) },
                                onFocusClick = { viewModel.startFocusSession(event) }
                            )
                        }
                    }
                }
            }
        }

        // Today's All Events Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S EVENTS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${todayEvents.size} events",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )
            }
        }

        if (todayEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color(0xFF10121C) else Color(0xFFF8FAFC))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.EventAvailable,
                            contentDescription = "No events",
                            tint = primaryColor,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No events scheduled for today",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + at the top right or quick add below",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(todayEvents) { event ->
                EventCard(
                    event = event,
                    isHero = false,
                    is24Hour = is24Hour,
                    onClick = { viewModel.viewEvent(event) },
                    onFocusClick = { viewModel.startFocusSession(event) }
                )
            }
        }

        // Quick Tasks Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUICK TASKS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${pendingTasks.size} pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (pendingTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) Color(0xFF10121C) else Color(0xFFF8FAFC))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All tasks completed! Great job.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pendingTasks.take(4).forEach { task ->
                            TaskCard(
                                task = task,
                                onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                                onClick = { viewModel.openEditTask(task) }
                            )
                        }
                    }
                }
            }
        }
    }
}
