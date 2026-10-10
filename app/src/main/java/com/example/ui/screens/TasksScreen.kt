package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.ui.components.TaskCard
import com.example.ui.theme.LocalArcTheme
import com.example.ui.theme.getCategoryColor
import com.example.viewmodel.ArcCalendarViewModel

@Composable
fun TasksScreen(
    viewModel: ArcCalendarViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    val isDark = theme.isDark
    val primaryColor = theme.preset.primary

    val allTasks by viewModel.allTasks.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf<String?>("all") }

    val filteredTasks = remember(allTasks, selectedCategoryFilter) {
        if (selectedCategoryFilter == null || selectedCategoryFilter == "all") {
            allTasks
        } else {
            allTasks.filter { it.categoryId.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    val todoTasks = filteredTasks.filter { !it.isCompleted }
    val completedTasks = filteredTasks.filter { it.isCompleted }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("tasks_screen")
    ) {
        // Category Filter Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val isSelected = selectedCategoryFilter == "all"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) primaryColor
                            else if (isDark) Color(0xFF141724)
                            else Color(0xFFEEF2F6)
                        )
                        .clickable { selectedCategoryFilter = "all" }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("task_filter_all"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All (${allTasks.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(allCategories) { cat ->
                val isSelected = selectedCategoryFilter == cat.id
                val catColor = getCategoryColor(cat.id, cat.colorHex)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) catColor
                            else if (isDark) Color(0xFF141724)
                            else Color(0xFFEEF2F6)
                        )
                        .clickable { selectedCategoryFilter = cat.id }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("task_filter_${cat.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: To Do
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TO DO",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${todoTasks.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            if (todoTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) Color(0xFF10121C) else Color(0xFFF8FAFC))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pending tasks in this category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(todoTasks) { task ->
                    TaskCard(
                        task = task,
                        onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                        onClick = { viewModel.openEditTask(task) }
                    )
                }
            }

            // Header: Completed
            if (completedTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMPLETED",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "${completedTasks.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(completedTasks) { task ->
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
