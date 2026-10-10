package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalArcTheme
import com.example.ui.theme.getCategoryColor
import com.example.viewmodel.ArcCalendarViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun FocusModeScreen(
    viewModel: ArcCalendarViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    val isDark = theme.isDark

    val focusSession by viewModel.focusSession.collectAsState()
    val session = focusSession ?: return
    val categoryColor = getCategoryColor(session.categoryId)

    // Timer coroutine tick
    LaunchedEffect(session.isRunning, session.isPaused) {
        while (isActive && session.isRunning && !session.isPaused && session.remainingSeconds > 0) {
            delay(1000L)
            viewModel.updateFocusTick()
        }
    }

    val totalSec = session.totalSeconds.coerceAtLeast(1)
    val remSec = session.remainingSeconds.coerceAtLeast(0)
    val progress = (remSec.toFloat() / totalSec).coerceIn(0f, 1f)

    val minutes = remSec / 60
    val seconds = remSec % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(if (isDark) Color(0xFF07080E) else Color(0xFFF1F5F9))
            .padding(24.dp)
            .testTag("focus_mode_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(categoryColor.copy(alpha = 0.25f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = session.categoryId.uppercase(),
                        color = categoryColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { viewModel.exitFocusSession() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF161928) else Color(0xFFE2E8F0))
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Exit")
                }
            }

            // Central Circular Arc Timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        // Background Track
                        drawCircle(
                            color = if (isDark) Color(0xFF161928) else Color(0xFFE2E8F0),
                            style = Stroke(width = strokeWidth)
                        )
                        // Progress Arc
                        drawArc(
                            color = categoryColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (session.isPaused) "PAUSED" else "FOCUSING",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (session.isPaused) Color(0xFFF59E0B) else categoryColor,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = session.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Bottom Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Pause / Resume Button
                Button(
                    onClick = { viewModel.togglePauseFocus() },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF1B1F32) else Color(0xFFE2E8F0)
                    )
                ) {
                    Icon(
                        imageVector = if (session.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (session.isPaused) "RESUME" else "PAUSE",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Complete Button
                Button(
                    onClick = { viewModel.completeFocusSession() },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = categoryColor)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COMPLETE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
