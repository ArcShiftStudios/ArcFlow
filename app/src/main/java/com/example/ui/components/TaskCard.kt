package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TaskEntity
import com.example.ui.theme.LocalArcTheme
import com.example.ui.theme.getCategoryColor

@Composable
fun TaskCard(
    task: TaskEntity,
    onToggleCompleted: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    val isDark = theme.isDark
    val categoryColor = getCategoryColor(task.categoryId)

    val priorityColor = when (task.priority) {
        3 -> Color(0xFFEF4444) // Urgent - Red
        2 -> Color(0xFFF59E0B) // High - Orange
        1 -> Color(0xFF3B82F6) // Medium - Blue
        else -> Color(0xFF64748B) // Low - Gray
    }

    val cardBg = if (isDark) {
        if (task.isCompleted) Color(0xFF0E1017) else Color(0xFF141622)
    } else {
        if (task.isCompleted) Color(0xFFF1F5F9) else Color(0xFFFFFFFF)
    }

    val cardBorder = if (isDark) {
        if (task.isCompleted) Color(0xFF1E2433) else Color(0xFF242B40)
    } else {
        if (task.isCompleted) Color(0xFFE2E8F0) else Color(0xFFCBD5E1)
    }

    val checkBg by animateColorAsState(
        targetValue = if (task.isCompleted) categoryColor else Color.Transparent,
        animationSpec = spring(),
        label = "task_check_bg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("task_card_${task.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom animated Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(checkBg)
                .border(
                    width = 2.dp,
                    color = if (task.isCompleted) categoryColor else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .clickable(onClick = onToggleCompleted)
                .testTag("task_checkbox_${task.id}"),
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (task.isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (task.isCompleted) 0.5f else 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Category Tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(categoryColor.copy(alpha = if (isDark) 0.20f else 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = task.categoryId.replaceFirstChar { it.uppercase() },
                color = categoryColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        }

        if (task.priority >= 2) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = "Priority",
                tint = priorityColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
