package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalArcTheme
import com.example.ui.theme.getCategoryColor
import com.example.viewmodel.ArcCalendarViewModel

@Composable
fun SettingsScreen(
    viewModel: ArcCalendarViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = LocalArcTheme.current
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = theme.preset.primary

    val allCalendars by viewModel.allCalendars.collectAsState()
    val firstDayOfWeek by viewModel.firstDayOfWeek.collectAsState()
    val is24Hour by viewModel.is24HourFormat.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    var showAddCalendarDialog by remember { mutableStateOf(false) }
    var newCalendarName by remember { mutableStateOf("") }

    val textColor = colorScheme.onSurface
    val subTextColor = colorScheme.onSurfaceVariant

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Branding Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.22f),
                                theme.preset.accentGradientEnd.copy(alpha = 0.12f),
                                colorScheme.surfaceContainer
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.45f),
                                theme.preset.accentGradientEnd.copy(alpha = 0.25f)
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        colorScheme.surfaceVariant
                                    )
                                )
                            )
                            .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arc_logo),
                            contentDescription = "Arc Logo",
                            modifier = Modifier.size(28.dp),
                            colorFilter = ColorFilter.tint(primaryColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "ArcFlow",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "by ArcShift Studios • Sibling to ArcCalc",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor
                        )
                    }
                }
            }
        }

        // Calendars Management Section
        item {
            SettingsSectionCard(title = "Calendars") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    allCalendars.forEach { cal ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(getCategoryColor("custom", cal.colorHex))
                                )
                                Text(
                                    text = cal.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                            }

                            Switch(
                                checked = cal.isVisible,
                                onCheckedChange = { viewModel.toggleCalendarVisibility(cal) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorScheme.onPrimary,
                                    checkedTrackColor = primaryColor,
                                    uncheckedThumbColor = colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = colorScheme.surfaceVariant,
                                    uncheckedBorderColor = colorScheme.outline
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showAddCalendarDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor.copy(alpha = 0.15f),
                            contentColor = primaryColor
                        ),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = primaryColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add New Calendar",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            }
        }

        // Appearance & Themes Shortcut
        item {
            SettingsSectionCard(title = "Appearance & Customization") {
                SettingsClickableItem(
                    icon = Icons.Filled.Palette,
                    title = "Themes & Visuals",
                    subtitle = "22+ Presets, dynamic daily accents, gesture particles",
                    onClick = { viewModel.openThemes() }
                )
            }
        }

        // General Preferences: Time Format & Week Start
        item {
            SettingsSectionCard(title = "Date & Time Preferences") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "24-Hour Time Format",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            Text(
                                text = if (is24Hour) "Using 24-hour clock (15:00)" else "Using 12-hour clock (3:00 PM)",
                                style = MaterialTheme.typography.bodySmall,
                                color = subTextColor
                            )
                        }
                        Switch(
                            checked = is24Hour,
                            onCheckedChange = { viewModel.toggleTimeFormat() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorScheme.onPrimary,
                                checkedTrackColor = primaryColor,
                                uncheckedThumbColor = colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = colorScheme.surfaceVariant,
                                uncheckedBorderColor = colorScheme.outline
                            )
                        )
                    }

                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "First Day of Week",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            Text(
                                text = "Currently: $firstDayOfWeek",
                                style = MaterialTheme.typography.bodySmall,
                                color = subTextColor
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.setFirstDayOfWeek(if (firstDayOfWeek == "MONDAY") "SUNDAY" else "MONDAY")
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = if (firstDayOfWeek == "MONDAY") "Monday" else "Sunday",
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        // Import & Export / Backup
        item {
            SettingsSectionCard(title = "Data & Backup") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsClickableItem(
                        icon = Icons.Outlined.FileDownload,
                        title = "Export Calendar (.ics)",
                        subtitle = "Standard iCalendar format for backup or external use",
                        onClick = { showExportDialog = true }
                    )

                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.2f))

                    SettingsClickableItem(
                        icon = Icons.Outlined.FileUpload,
                        title = "Import Calendar (.ics)",
                        subtitle = "Import events from .ics text or file",
                        onClick = { showImportDialog = true }
                    )
                }
            }
        }

        // About ArcShift Studios
        item {
            SettingsSectionCard(title = "About") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ArcFlow v1.0.0",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Designed & crafted by ArcShift Studios as a private personal command center. All plans, tasks, and calendar events are stored 100% locally on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = subTextColor
                    )
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        val icsText = remember { viewModel.exportIcs() }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = colorScheme.surfaceContainerHigh,
            title = { Text("Exported iCalendar (.ics)", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Your calendar data has been formatted in standard iCalendar syntax:", color = textColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = icsText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = colorScheme.onSurface),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("ArcFlow ICS", icsText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied .ics to clipboard!", Toast.LENGTH_SHORT).show()
                    showExportDialog = false
                }) {
                    Text("Copy to Clipboard", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = subTextColor)
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = colorScheme.surfaceContainerHigh,
            title = { Text("Import iCalendar (.ics)", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Paste .ics calendar text below:", color = textColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        placeholder = { Text("BEGIN:VCALENDAR...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importText.isNotBlank()) {
                            viewModel.importIcs(importText) { count ->
                                Toast.makeText(context, "Imported $count events!", Toast.LENGTH_SHORT).show()
                            }
                            showImportDialog = false
                            importText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = colorScheme.onPrimary)
                ) {
                    Text("Import", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = subTextColor)
                }
            }
        )
    }

    // Add Calendar Dialog
    if (showAddCalendarDialog) {
        AlertDialog(
            onDismissRequest = { showAddCalendarDialog = false },
            containerColor = colorScheme.surfaceContainerHigh,
            title = { Text("New Calendar", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCalendarName,
                    onValueChange = { newCalendarName = it },
                    label = { Text("Calendar Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCalendarName.isNotBlank()) {
                            viewModel.saveCategory("cal_${System.currentTimeMillis()}", newCalendarName, "#38BDF8", "event")
                            showAddCalendarDialog = false
                            newCalendarName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = colorScheme.onPrimary)
                ) {
                    Text("Create", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCalendarDialog = false }) {
                    Text("Cancel", color = subTextColor)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colorScheme.surfaceContainer)
            .border(1.dp, colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val theme = LocalArcTheme.current
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = theme.preset.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
