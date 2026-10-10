package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
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
import com.example.ui.theme.ArcThemePreset
import com.example.ui.theme.ArcThemes
import com.example.ui.theme.LocalArcTheme
import com.example.viewmodel.ArcCalendarViewModel

@Composable
fun ThemesScreen(
    viewModel: ArcCalendarViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    val colorScheme = MaterialTheme.colorScheme

    val currentThemePresetId by viewModel.themePresetId.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicAccents by viewModel.dynamicDailyAccents.collectAsState()
    val gestureParticles by viewModel.gestureParticles.collectAsState()
    val ambientDots by viewModel.ambientDots.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("themes_screen"),
        color = colorScheme.surface,
        contentColor = colorScheme.onSurface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = "Themes",
                                tint = colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "THEMES & VISUALS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Appearance Mode (System, Dark, Light)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Appearance Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SYSTEM", "DARK", "LIGHT").forEach { mode ->
                            val isSelected = themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) colorScheme.primary
                                        else colorScheme.surface
                                    )
                                    .border(
                                        width = if (isSelected) 0.dp else 1.dp,
                                        color = if (isSelected) colorScheme.primary else colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (mode == "SYSTEM") "System" else if (mode == "DARK") "Dark" else "Light",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Effects Toggles
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Expressive Effects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic Daily Accents Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dynamic Daily Accents",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Subtle tonal adaptation throughout the day",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = dynamicAccents,
                            onCheckedChange = { viewModel.toggleDynamicAccents() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorScheme.onPrimary,
                                checkedTrackColor = colorScheme.primary,
                                uncheckedThumbColor = colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = colorScheme.surfaceVariant,
                                uncheckedBorderColor = colorScheme.outline
                            )
                        )
                    }

                    HorizontalDivider(
                        color = colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Gesture Particle Trail Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gesture Particle Dots",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Soft flowing particle trail on tap & drag",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = gestureParticles,
                            onCheckedChange = { viewModel.toggleGestureParticles() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorScheme.onPrimary,
                                checkedTrackColor = colorScheme.primary,
                                uncheckedThumbColor = colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = colorScheme.surfaceVariant,
                                uncheckedBorderColor = colorScheme.outline
                            )
                        )
                    }

                    HorizontalDivider(
                        color = colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Ambient Background Dots Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ambient Constellation Dots",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Soft floating background micro-dots",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = ambientDots,
                            onCheckedChange = { viewModel.toggleAmbientDots() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorScheme.onPrimary,
                                checkedTrackColor = colorScheme.primary,
                                uncheckedThumbColor = colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = colorScheme.surfaceVariant,
                                uncheckedBorderColor = colorScheme.outline
                            )
                        )
                    }
                }
            }

            // Themes Presets Grid Header
            item {
                Text(
                    text = "COLOR THEMES (${ArcThemes.presets.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            // Themes Grid
            items(ArcThemes.presets.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { preset ->
                        val isSelected = currentThemePresetId == preset.id
                        ThemePresetCard(
                            preset = preset,
                            isSelected = isSelected,
                            onClick = { viewModel.setThemePreset(preset.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ThemePresetCard(
    preset: ArcThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val bgGradient = Brush.linearGradient(
        listOf(preset.accentGradientStart, preset.accentGradientEnd)
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) preset.primary else colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("theme_card_${preset.id}")
    ) {
        // Mini Preview Swatch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgGradient)
                .padding(10.dp)
        ) {
            // Little preview cards inside swatch
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.35f))
                )
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = preset.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = preset.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )
    }
}
