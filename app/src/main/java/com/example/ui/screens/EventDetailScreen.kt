package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.theme.LocalArcTheme
import com.example.ui.theme.getCategoryColor
import com.example.viewmodel.ArcCalendarViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun EventDetailScreen(
    event: EventEntity,
    viewModel: ArcCalendarViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    val colorScheme = MaterialTheme.colorScheme
    val categoryColor = getCategoryColor(event.categoryId, event.colorHex)
    val is24Hour by viewModel.is24HourFormat.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val eventDate = LocalDate.ofEpochDay(event.dateEpochDay)
    val dateStr = "${eventDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, ${eventDate.dayOfMonth} ${eventDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${eventDate.year}"

    val startH = event.startMinuteOfDay / 60
    val startM = event.startMinuteOfDay % 60
    val endH = event.endMinuteOfDay / 60
    val endM = event.endMinuteOfDay % 60

    val timeStr = if (is24Hour) {
        String.format("%02d:%02d — %02d:%02d", startH, startM, endH, endM)
    } else {
        val startAmpm = if (startH >= 12) "PM" else "AM"
        val startH12 = if (startH % 12 == 0) 12 else startH % 12
        val endAmpm = if (endH >= 12) "PM" else "AM"
        val endH12 = if (endH % 12 == 0) 12 else endH % 12
        String.format("%d:%02d %s — %d:%02d %s", startH12, startM, startAmpm, endH12, endM, endAmpm)
    }

    val durationMin = (event.endMinuteOfDay - event.startMinuteOfDay).coerceAtLeast(0)
    val durationStr = if (durationMin >= 60) "${durationMin / 60}h ${durationMin % 60}m" else "${durationMin}m"

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("event_detail_screen"),
        color = colorScheme.surface,
        contentColor = colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar (Back, Pin, Options)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleEventPin(event) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (event.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (event.isPinned) Color(0xFFF59E0B) else colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.openEditEvent(event)
                            onClose()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = colorScheme.error
                        )
                    }
                }
            }

            // Hero Category Card Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                categoryColor.copy(alpha = 0.25f),
                                colorScheme.surfaceContainer
                            )
                        )
                    )
                    .border(1.dp, categoryColor.copy(alpha = 0.45f), RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(categoryColor.copy(alpha = 0.3f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = event.categoryId.uppercase(),
                            color = categoryColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = durationStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                if (event.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Start Focus Action Button
            Button(
                onClick = {
                    viewModel.startFocusSession(event)
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = categoryColor,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START FOCUS MODE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colorScheme.surfaceContainer)
                    .border(1.dp, colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Date & Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$dateStr • $timeStr",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.2f))

                // Reminder
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Reminder",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        val reminderText = if (event.reminderMinutesBefore < 0) "No reminder"
                        else if (event.reminderMinutesBefore == 0) "At event start"
                        else "${event.reminderMinutesBefore} minutes before"
                        Text(
                            text = reminderText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        )
                    }
                }

                if (event.location.isNotBlank()) {
                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.2f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Location",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = event.location,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                }

                if (event.notes.isNotBlank()) {
                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.2f))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Outlined.Notes,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = event.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = colorScheme.surfaceContainerHigh,
            title = { Text("Delete Event", color = colorScheme.onSurface) },
            text = { Text("Are you sure you want to delete '${event.title}'? This action cannot be undone.", color = colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEvent(event)
                        showDeleteConfirm = false
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError)
                ) {
                    Text("Delete", color = colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
