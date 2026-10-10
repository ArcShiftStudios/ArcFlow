package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LocalArcTheme

private data class AmbientDot(
    val initialXRatio: Float,
    val initialYRatio: Float,
    val radius: Float,
    val speed: Float,
    val baseAlpha: Float
)

@Composable
fun AmbientDotsBackground(
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    if (!theme.ambientDotsEnabled) return

    val dots = remember {
        listOf(
            AmbientDot(0.12f, 0.15f, 2.2f, 0.08f, 0.18f),
            AmbientDot(0.85f, 0.22f, 1.8f, 0.06f, 0.15f),
            AmbientDot(0.28f, 0.45f, 2.5f, 0.10f, 0.22f),
            AmbientDot(0.72f, 0.58f, 1.6f, 0.07f, 0.14f),
            AmbientDot(0.40f, 0.78f, 2.0f, 0.09f, 0.19f),
            AmbientDot(0.88f, 0.85f, 2.4f, 0.05f, 0.16f),
            AmbientDot(0.15f, 0.92f, 1.7f, 0.08f, 0.15f)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_dots")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val dotColor = if (theme.isDark) Color.White else Color(0xFF1E293B)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        for (dot in dots) {
            val yOffset = ((dot.initialYRatio + phase * dot.speed) % 1f) * height
            val xOffset = dot.initialXRatio * width
            drawCircle(
                color = dotColor.copy(alpha = dot.baseAlpha),
                radius = dot.radius,
                center = Offset(xOffset, yOffset)
            )
        }
    }
}
