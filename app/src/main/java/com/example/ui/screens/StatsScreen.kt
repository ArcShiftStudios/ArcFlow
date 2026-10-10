package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.ui.theme.LocalArcTheme
import com.example.ui.theme.getCategoryColor
import com.example.viewmodel.ArcCalendarViewModel

@Composable
fun StatsScreen(
    viewModel: ArcCalendarViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    val isDark = theme.isDark
    val primaryColor = theme.preset.primary

    val allEvents by viewModel.allEvents.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    val totalEventsCount = allEvents.size
    val totalTaskCount = allTasks.size
    val completedTasksCount = allTasks.count { it.isCompleted }

    // Calculate hours by category
    val categoryMinutesMap = remember(allEvents) {
        val map = mutableMapOf<String, Int>()
        for (ev in allEvents) {
            val dur = (ev.endMinuteOfDay - ev.startMinuteOfDay).coerceAtLeast(0)
            map[ev.categoryId] = (map[ev.categoryId] ?: 0) + dur
        }
        map
    }

    val totalMinutes = categoryMinutesMap.values.sum().coerceAtLeast(1)
    val studyHours = (categoryMinutesMap["study"] ?: 0) / 60

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = "TIME INSIGHTS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Weekly Overview & Distribution",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Summary Metric Cards: Total Events & Study Time
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Events Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    primaryColor.copy(alpha = if (isDark) 0.16f else 0.08f),
                                    if (isDark) Color(0xFF10121C) else Color(0xFFFFFFFF)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    primaryColor.copy(alpha = if (isDark) 0.35f else 0.20f),
                                    if (isDark) Color(0xFF1E2436) else Color(0xFFE2E8F0)
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total events",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowOutward,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "$totalEventsCount",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "↑ 20% vs last week",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }

                // Study / Focus Time Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    theme.preset.accentGradientEnd.copy(alpha = if (isDark) 0.16f else 0.08f),
                                    if (isDark) Color(0xFF10121C) else Color(0xFFFFFFFF)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    theme.preset.accentGradientEnd.copy(alpha = if (isDark) 0.35f else 0.20f),
                                    if (isDark) Color(0xFF1E2436) else Color(0xFFE2E8F0)
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Study time",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${studyHours}h",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "↑ 35% scheduled",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        }

        // Time Distribution Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (isDark) Color(0xFF10121C) else Color(0xFFFFFFFF))
                    .border(1.dp, if (isDark) Color(0xFF1E2436) else Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Time distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                val categories = listOf("study", "projects", "personal", "health", "important", "neutral")
                categories.forEach { catId ->
                    val mins = categoryMinutesMap[catId] ?: 0
                    if (mins > 0 || catId in listOf("study", "projects", "personal")) {
                        val hours = mins / 60
                        val percentage = (mins.toFloat() / totalMinutes * 100f).toInt()
                        val catColor = getCategoryColor(catId)

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                    Text(
                                        text = catId.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "${hours}h • $percentage%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { (percentage / 100f).coerceIn(0.04f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = catColor,
                                trackColor = if (isDark) Color(0xFF1A1F30) else Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
        }

        // Completed Tasks Progress Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark) Color(0xFF10121C) else Color(0xFFFFFFFF))
                    .border(1.dp, if (isDark) Color(0xFF1E2436) else Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Completed tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "$completedTasksCount / $totalTaskCount",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val taskPercent = if (totalTaskCount > 0) completedTasksCount.toFloat() / totalTaskCount else 0f

                LinearProgressIndicator(
                    progress = { taskPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Color(0xFF10B981),
                    trackColor = if (isDark) Color(0xFF1E2436) else Color(0xFFE2E8F0)
                )
            }
        }
    }
}
