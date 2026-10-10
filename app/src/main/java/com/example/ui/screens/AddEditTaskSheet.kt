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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TaskEntity
import com.example.ui.theme.LocalArcTheme
import com.example.ui.theme.getCategoryColor
import com.example.viewmodel.ArcCalendarViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskSheet(
    viewModel: ArcCalendarViewModel,
    existingTask: TaskEntity? = null,
    onDismiss: () -> Unit
) {
    val theme = LocalArcTheme.current
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = theme.preset.primary

    val allCategories by viewModel.allCategories.collectAsState()

    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var description by remember { mutableStateOf(existingTask?.description ?: "") }
    var selectedCategoryId by remember { mutableStateOf(existingTask?.categoryId ?: "study") }
    var priority by remember { mutableStateOf(existingTask?.priority ?: 1) } // 0=low, 1=medium, 2=high, 3=urgent
    var isPinned by remember { mutableStateOf(existingTask?.isPinned ?: false) }

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
                    text = if (existingTask == null) "NEW TASK" else "EDIT TASK",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    letterSpacing = 1.sp
                )

                Row {
                    if (existingTask != null) {
                        IconButton(onClick = {
                            viewModel.deleteTask(existingTask)
                            onDismiss()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = colorScheme.onSurface
                        )
                    }
                }
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                placeholder = { Text("e.g. Complete math worksheet") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
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

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Details (Optional)") },
                placeholder = { Text("Add instructions or notes...") },
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

            // Category Selection
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

            // Priority Selection
            Column {
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to "Low",
                        1 to "Medium",
                        2 to "High",
                        3 to "Urgent"
                    ).forEach { (pVal, label) ->
                        val isSelected = priority == pVal
                        val pColor = when (pVal) {
                            3 -> Color(0xFFEF4444)
                            2 -> Color(0xFFF59E0B)
                            1 -> Color(0xFF3B82F6)
                            else -> Color(0xFF64748B)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) pColor
                                    else colorScheme.surfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) pColor else colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { priority = pVal }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Save Task Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.saveTask(
                            title = title,
                            description = description,
                            categoryId = selectedCategoryId,
                            dueDateEpochDay = LocalDate.now().toEpochDay(),
                            dueTimeMinute = 18 * 60,
                            priority = priority,
                            isPinned = isPinned
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_task_button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (existingTask == null) "ADD TASK" else "SAVE TASK",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimary
                )
            }
        }
    }
}
