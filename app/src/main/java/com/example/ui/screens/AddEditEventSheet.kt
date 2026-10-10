package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.service.EventParser
import com.example.ui.theme.LocalArcTheme
import com.example.ui.theme.getCategoryColor
import com.example.viewmodel.ArcCalendarViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventSheet(
    viewModel: ArcCalendarViewModel,
    existingEvent: EventEntity? = null,
    onDismiss: () -> Unit
) {
    val theme = LocalArcTheme.current
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = theme.preset.primary

    val allCategories by viewModel.allCategories.collectAsState()
    val allCalendars by viewModel.allCalendars.collectAsState()
    val initialDate by viewModel.selectedDate.collectAsState()

    var nlpInput by remember { mutableStateOf("") }

    var title by remember { mutableStateOf(existingEvent?.title ?: "") }
    var description by remember { mutableStateOf(existingEvent?.description ?: "") }
    var selectedCategoryId by remember { mutableStateOf(existingEvent?.categoryId ?: "study") }
    var selectedCalendarId by remember { mutableStateOf(existingEvent?.calendarId ?: 1L) }
    var targetDate by remember {
        mutableStateOf(
            if (existingEvent != null) LocalDate.ofEpochDay(existingEvent.dateEpochDay) else initialDate
        )
    }
    var startHour by remember { mutableStateOf(existingEvent?.let { it.startMinuteOfDay / 60 } ?: 15) }
    var startMin by remember { mutableStateOf(existingEvent?.let { it.startMinuteOfDay % 60 } ?: 0) }
    var endHour by remember { mutableStateOf(existingEvent?.let { it.endMinuteOfDay / 60 } ?: 16) }
    var endMin by remember { mutableStateOf(existingEvent?.let { it.endMinuteOfDay % 60 } ?: 30) }

    var recurrence by remember { mutableStateOf(existingEvent?.recurrence ?: "NONE") }
    var reminderMinutes by remember { mutableStateOf(existingEvent?.reminderMinutesBefore ?: 15) }
    var location by remember { mutableStateOf(existingEvent?.location ?: "") }
    var notes by remember { mutableStateOf(existingEvent?.notes ?: "") }
    var isPinned by remember { mutableStateOf(existingEvent?.isPinned ?: false) }

    val categoryColor = getCategoryColor(selectedCategoryId)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surfaceContainerHigh,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingEvent == null) "CREATE EVENT" else "EDIT EVENT",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    letterSpacing = 1.sp
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = colorScheme.onSurface
                    )
                }
            }

            // NLP Quick Add Bar (Only for new events)
            if (existingEvent == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    primaryColor.copy(alpha = 0.12f),
                                    colorScheme.surfaceVariant
                                )
                            )
                        )
                        .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Quick Add",
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Natural Language Quick Add",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nlpInput,
                        onValueChange = { nlpInput = it },
                        placeholder = { Text("e.g. Physics tomorrow 5pm for 1 hour") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            if (nlpInput.isNotBlank()) {
                                IconButton(onClick = {
                                    val parsed = EventParser.parse(nlpInput)
                                    title = parsed.title
                                    description = parsed.description
                                    selectedCategoryId = parsed.categoryId
                                    targetDate = LocalDate.ofEpochDay(parsed.dateEpochDay)
                                    startHour = parsed.startMinuteOfDay / 60
                                    startMin = parsed.startMinuteOfDay % 60
                                    endHour = parsed.endMinuteOfDay / 60
                                    endMin = parsed.endMinuteOfDay % 60
                                    recurrence = parsed.recurrence
                                    reminderMinutes = parsed.reminderMinutesBefore
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Fill",
                                        tint = primaryColor
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                placeholder = { Text("e.g. Physics Revision") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_title_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = colorScheme.onSurfaceVariant
                )
            )

            // Category Chips Selection
            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allCategories) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        val catColor = getCategoryColor(cat.id, cat.colorHex)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) catColor
                                    else colorScheme.surfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) catColor else colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedCategoryId = cat.id }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Date Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${targetDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, ${targetDate.dayOfMonth} ${targetDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = { targetDate = LocalDate.now() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Today")
                    }
                    FilledTonalButton(
                        onClick = { targetDate = LocalDate.now().plusDays(1) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tomorrow")
                    }
                }
            }

            // Time Selector (Start and End)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.surfaceVariant)
                            .border(1.dp, colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable {
                                startHour = (startHour + 1) % 24
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%02d:%02d", startHour, startMin),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }

                // End Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "End Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.surfaceVariant)
                            .border(1.dp, colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable {
                                endHour = (endHour + 1) % 24
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%02d:%02d", endHour, endMin),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            }

            // Recurrence Options
            Column {
                Text(
                    text = "Repeat",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("NONE", "DAILY", "WEEKDAYS", "WEEKLY", "MONTHLY")) { rec ->
                        val isSelected = recurrence == rec
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) primaryColor
                                    else colorScheme.surfaceVariant
                                )
                                .border(1.dp, if (isSelected) primaryColor else colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .clickable { recurrence = rec }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = rec,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Reminder Options
            Column {
                Text(
                    text = "Reminder",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        listOf(
                            -1 to "None",
                            0 to "At time",
                            5 to "5m before",
                            15 to "15m before",
                            30 to "30m before",
                            60 to "1h before"
                        )
                    ) { (mins, label) ->
                        val isSelected = reminderMinutes == mins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) primaryColor
                                    else colorScheme.surfaceVariant
                                )
                                .border(1.dp, if (isSelected) primaryColor else colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .clickable { reminderMinutes = mins }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (Optional)") },
                placeholder = { Text("e.g. Classroom 3B / Zoom") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = colorScheme.onSurfaceVariant,
                    focusedLeadingIconColor = primaryColor,
                    unfocusedLeadingIconColor = colorScheme.onSurfaceVariant
                ),
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = null)
                }
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                placeholder = { Text("Additional event notes...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = colorScheme.onSurfaceVariant
                )
            )

            // Pin toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pin to Today Hero Section",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Switch(
                    checked = isPinned,
                    onCheckedChange = { isPinned = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.onPrimary,
                        checkedTrackColor = primaryColor,
                        uncheckedThumbColor = colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = colorScheme.surfaceVariant,
                        uncheckedBorderColor = colorScheme.outline
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Action Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.saveEvent(
                            title = title,
                            description = description,
                            categoryId = selectedCategoryId,
                            calendarId = selectedCalendarId,
                            dateEpochDay = targetDate.toEpochDay(),
                            startMinute = startHour * 60 + startMin,
                            endMinute = endHour * 60 + endMin,
                            isPinned = isPinned,
                            recurrence = recurrence,
                            reminderMinutes = reminderMinutes,
                            location = location,
                            notes = notes
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_event_button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (existingEvent == null) "CREATE EVENT" else "SAVE CHANGES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimary
                )
            }
        }
    }
}
