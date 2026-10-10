package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import java.time.LocalTime

data class ArcThemeState(
    val preset: ArcThemePreset,
    val isDark: Boolean,
    val accentGradient: Brush,
    val dynamicDailyAccents: Boolean = true,
    val gestureParticlesEnabled: Boolean = true,
    val ambientDotsEnabled: Boolean = true
)

val LocalArcTheme = staticCompositionLocalOf<ArcThemeState> {
    error("No ArcThemeState provided")
}

@Composable
fun ArcCalendarTheme(
    themePresetId: String = "arc_blue",
    themeMode: String = "SYSTEM", // "SYSTEM", "DARK", "LIGHT"
    dynamicDailyAccents: Boolean = true,
    gestureParticlesEnabled: Boolean = true,
    ambientDotsEnabled: Boolean = true,
    customPrimaryHex: String? = null,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    var basePreset = ArcThemes.getPreset(themePresetId)
    if (!customPrimaryHex.isNullOrBlank()) {
        try {
            val customColor = Color(android.graphics.Color.parseColor(customPrimaryHex))
            basePreset = basePreset.copy(
                name = "Custom",
                primary = customColor,
                secondary = customColor.copy(alpha = 0.8f),
                accentGradientStart = customColor,
                accentGradientEnd = customColor.copy(alpha = 0.6f)
            )
        } catch (_: Exception) {}
    }

    // Dynamic daily accent calculation
    val finalPreset = remember(basePreset, dynamicDailyAccents) {
        if (!dynamicDailyAccents) basePreset
        else {
            val hour = LocalTime.now().hour
            when (hour) {
                in 5..11 -> basePreset // Morning crisp
                in 12..17 -> basePreset // Afternoon
                in 18..21 -> basePreset.copy(
                    primary = basePreset.primary.copy(red = (basePreset.primary.red * 1.05f).coerceAtMost(1f))
                ) // Sunset warm shift
                else -> basePreset.copy(
                    darkBackground = basePreset.darkBackground.copy(alpha = 1.0f)
                ) // Night deep
            }
        }
    }

    val colorScheme = remember(finalPreset, isDark) {
        if (isDark) {
            darkColorScheme(
                primary = finalPreset.primary,
                onPrimary = Color.White,
                primaryContainer = finalPreset.darkSurfaceVariant,
                onPrimaryContainer = finalPreset.primary,
                secondary = finalPreset.secondary,
                onSecondary = Color.White,
                secondaryContainer = finalPreset.darkSurfaceVariant,
                onSecondaryContainer = finalPreset.secondary,
                tertiary = finalPreset.tertiary,
                onTertiary = Color.White,
                background = finalPreset.darkBackground,
                onBackground = Color.White,
                surface = finalPreset.darkSurface,
                onSurface = Color.White,
                surfaceVariant = finalPreset.darkSurfaceVariant,
                onSurfaceVariant = Color(0xFFCBD5E1),
                outline = Color(0xFF334155),
                outlineVariant = Color(0xFF242B3D)
            )
        } else {
            lightColorScheme(
                primary = finalPreset.primary,
                onPrimary = Color.White,
                primaryContainer = finalPreset.lightSurfaceVariant,
                onPrimaryContainer = finalPreset.primary,
                secondary = finalPreset.secondary,
                onSecondary = Color.White,
                secondaryContainer = finalPreset.lightSurfaceVariant,
                onSecondaryContainer = finalPreset.secondary,
                tertiary = finalPreset.tertiary,
                onTertiary = Color.White,
                background = finalPreset.lightBackground,
                onBackground = Color(0xFF0F172A),
                surface = finalPreset.lightSurface,
                onSurface = Color(0xFF0F172A),
                surfaceVariant = finalPreset.lightSurfaceVariant,
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFFCBD5E1),
                outlineVariant = Color(0xFFE2E8F0)
            )
        }
    }

    val accentGradient = Brush.linearGradient(
        colors = listOf(finalPreset.accentGradientStart, finalPreset.accentGradientEnd)
    )

    val arcThemeState = remember(finalPreset, isDark, accentGradient, dynamicDailyAccents, gestureParticlesEnabled, ambientDotsEnabled) {
        ArcThemeState(
            preset = finalPreset,
            isDark = isDark,
            accentGradient = accentGradient,
            dynamicDailyAccents = dynamicDailyAccents,
            gestureParticlesEnabled = gestureParticlesEnabled,
            ambientDotsEnabled = ambientDotsEnabled
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalArcTheme provides arcThemeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
