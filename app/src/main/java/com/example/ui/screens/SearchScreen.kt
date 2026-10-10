package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.components.EventCard
import com.example.ui.components.TaskCard
import com.example.ui.theme.LocalArcTheme
import com.example.viewmodel.ArcCalendarViewModel

@Composable
fun SearchScreen(
    viewModel: ArcCalendarViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = theme.preset.primary

    val searchQuery by viewModel.searchQuery.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val is24Hour by viewModel.is24HourFormat.collectAsState()

    val filteredEvents = remember(allEvents, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            allEvents.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.location.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.categoryId.lowercase().contains(q)
            }
        }
    }

    val filteredTasks = remember(allTasks, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            allTasks.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.categoryId.lowercase().contains(q)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen"),
        color = colorScheme.surface,
        contentColor = colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Search Header and Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search events, tasks, notes, locations...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = primaryColor)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear", tint = colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
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

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceVariant)
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = colorScheme.onSurfaceVariant)
                }
            }

            // Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (searchQuery.isBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Type keywords to search across ArcFlow",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    if (filteredEvents.isNotEmpty()) {
                        item {
                            Text(
                                text = "EVENTS (${filteredEvents.size})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                letterSpacing = 1.sp
                            )
                        }

                        items(filteredEvents) { ev ->
                            EventCard(
                                event = ev,
                                isHero = false,
                                is24Hour = is24Hour,
                                onClick = {
                                    viewModel.viewEvent(ev)
                                    onClose()
                                },
                                onFocusClick = {
                                    viewModel.startFocusSession(ev)
                                    onClose()
                                }
                            )
                        }
                    }

                    if (filteredTasks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "TASKS (${filteredTasks.size})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                letterSpacing = 1.sp
                            )
                        }

                        items(filteredTasks) { task ->
                            TaskCard(
                                task = task,
                                onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                                onClick = {
                                    viewModel.openEditTask(task)
                                    onClose()
                                }
                            )
                        }
                    }

                    if (filteredEvents.isEmpty() && filteredTasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No results found for '$searchQuery'",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
