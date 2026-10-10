package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.EventEntity
import com.example.ui.theme.LocalArcTheme
import com.example.ui.theme.getCategoryColor

@Composable
fun EventCard(
    event: EventEntity,
    onClick: () -> Unit,
    onFocusClick: (() -> Unit)? = null,
    isHero: Boolean = false,
    is24Hour: Boolean = true,
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    val isDark = theme.isDark
    val categoryColor = getCategoryColor(event.categoryId, event.colorHex)

    val startH = event.startMinuteOfDay / 60
    val startM = event.startMinuteOfDay % 60
    val endH = event.endMinuteOfDay / 60
    val endM = event.endMinuteOfDay % 60

    val timeFormatted = if (is24Hour) {
        String.format("%02d:%02d — %02d:%02d", startH, startM, endH, endM)
    } else {
        val startAmpm = if (startH >= 12) "PM" else "AM"
        val startH12 = if (startH % 12 == 0) 12 else startH % 12
        val endAmpm = if (endH >= 12) "PM" else "AM"
        val endH12 = if (endH % 12 == 0) 12 else endH % 12
        String.format("%d:%02d %s — %d:%02d %s", startH12, startM, startAmpm, endH12, endM, endAmpm)
    }

    val durationMin = (event.endMinuteOfDay - event.startMinuteOfDay).coerceAtLeast(0)
    val durationStr = if (durationMin >= 60) {
        val hours = durationMin / 60
        val mins = durationMin % 60
        if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
    } else {
        "${durationMin}m"
    }

    val cardBg = if (isDark) {
        if (isHero) {
            Brush.linearGradient(
                listOf(
                    categoryColor.copy(alpha = 0.30f),
                    Color(0xFF131522)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    categoryColor.copy(alpha = 0.16f),
                    Color(0xFF131520)
                )
            )
        }
    } else {
        if (isHero) {
            Brush.linearGradient(
                listOf(
                    categoryColor.copy(alpha = 0.22f),
                    Color(0xFFFFFFFF)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    categoryColor.copy(alpha = 0.12f),
                    Color(0xFFF8FAFC)
                )
            )
        }
    }

    val cardBorder = if (isHero) {
        categoryColor.copy(alpha = 0.55f)
    } else {
        categoryColor.copy(alpha = 0.25f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
            .testTag("event_card_${event.id}")
    ) {
        Column {
            // Header Row: Category Pill + Duration / Recurrence + Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(categoryColor.copy(alpha = if (isDark) 0.25f else 0.18f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = event.categoryId.uppercase(),
                            color = categoryColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x33FFFFFF) else Color(0x15000000))
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = durationStr,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    if (event.recurrence != "NONE") {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = "Recurring",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (event.isPinned) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (event.isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = event.title,
                style = if (isHero) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Time and Quick Focus Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "Time",
                        tint = categoryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                    )
                }

                if (onFocusClick != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(categoryColor)
                            .clickable(onClick = onFocusClick)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("event_focus_button_${event.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = "Focus",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Focus",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (event.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
