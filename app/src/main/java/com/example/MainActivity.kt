package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.ArcCalendarTheme
import com.example.viewmodel.ArcCalendarViewModel
import com.example.viewmodel.NavigationDestination

class MainActivity : ComponentActivity() {
    private val viewModel: ArcCalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val themePresetId by viewModel.themePresetId.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicAccents by viewModel.dynamicDailyAccents.collectAsState()
            val gestureParticles by viewModel.gestureParticles.collectAsState()
            val ambientDots by viewModel.ambientDots.collectAsState()
            val customPrimaryHex by viewModel.customPrimaryHex.collectAsState()

            // Request Notification Permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {}
                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            ArcCalendarTheme(
                themePresetId = themePresetId,
                themeMode = themeMode,
                dynamicDailyAccents = dynamicAccents,
                gestureParticlesEnabled = gestureParticles,
                ambientDotsEnabled = ambientDots,
                customPrimaryHex = customPrimaryHex
            ) {
                ArcCalendarApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ArcCalendarApp(viewModel: ArcCalendarViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsState()
    val isAddEventOpen by viewModel.isAddEventOpen.collectAsState()
    val editingEvent by viewModel.editingEvent.collectAsState()
    val isAddTaskOpen by viewModel.isAddTaskOpen.collectAsState()
    val editingTask by viewModel.editingTask.collectAsState()
    val viewingEvent by viewModel.viewingEvent.collectAsState()
    val focusSession by viewModel.focusSession.collectAsState()
    val isThemesOpen by viewModel.isThemesOpen.collectAsState()
    val isSearchOpen by viewModel.isSearchOpen.collectAsState()

    // Back button handling
    BackHandler(
        enabled = isAddEventOpen || isAddTaskOpen || viewingEvent != null ||
                focusSession != null || isThemesOpen || isSearchOpen ||
                currentDestination != NavigationDestination.TODAY
    ) {
        when {
            focusSession != null -> viewModel.exitFocusSession()
            viewingEvent != null -> viewModel.closeEventDetails()
            isAddEventOpen -> viewModel.closeAddEvent()
            isAddTaskOpen -> viewModel.closeAddTask()
            isThemesOpen -> viewModel.closeThemes()
            isSearchOpen -> viewModel.closeSearch()
            currentDestination != NavigationDestination.TODAY -> viewModel.navigateTo(NavigationDestination.TODAY)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
            // Subtle ambient background dots
            AmbientDotsBackground()

            // Main Application Layer
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    ArcTopBar(
                        title = "ArcFlow",
                        subtitle = "ArcShift Studios",
                        onSearchClick = { viewModel.openSearch() },
                        onThemesClick = { viewModel.openThemes() },
                        onAddClick = {
                            if (currentDestination == NavigationDestination.TASKS) {
                                viewModel.openAddTask()
                            } else {
                                viewModel.openAddEvent()
                            }
                        }
                    )
                },
                contentWindowInsets = WindowInsets.systemBars
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    // Content Screen switcher with smooth transitions
                    AnimatedContent(
                        targetState = currentDestination,
                        transitionSpec = {
                            fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                        },
                        label = "main_screen_transition"
                    ) { destination ->
                        when (destination) {
                            NavigationDestination.TODAY -> TodayScreen(viewModel = viewModel)
                            NavigationDestination.CALENDAR -> CalendarScreen(viewModel = viewModel)
                            NavigationDestination.TASKS -> TasksScreen(viewModel = viewModel)
                            NavigationDestination.STATS -> StatsScreen(viewModel = viewModel)
                            NavigationDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }

                    // Smooth Bottom Fade Gradient (content scrolls underneath smoothly)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )

                    // Floating Navigation Island
                    FloatingBottomNavIsland(
                        currentDestination = currentDestination,
                        onNavigate = { viewModel.navigateTo(it) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                    )
                }
            }

            // Modal Sheets and Overlays
            if (isAddEventOpen) {
                AddEditEventSheet(
                    viewModel = viewModel,
                    existingEvent = editingEvent,
                    onDismiss = { viewModel.closeAddEvent() }
                )
            }

            if (isAddTaskOpen) {
                AddEditTaskSheet(
                    viewModel = viewModel,
                    existingTask = editingTask,
                    onDismiss = { viewModel.closeAddTask() }
                )
            }

            if (viewingEvent != null) {
                EventDetailScreen(
                    event = viewingEvent!!,
                    viewModel = viewModel,
                    onClose = { viewModel.closeEventDetails() }
                )
            }

            if (focusSession != null) {
                FocusModeScreen(viewModel = viewModel)
            }

            if (isThemesOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    ThemesScreen(
                        viewModel = viewModel,
                        onClose = { viewModel.closeThemes() }
                    )
                }
            }

            if (isSearchOpen) {
                SearchScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.closeSearch() }
                )
            }
        }
}
