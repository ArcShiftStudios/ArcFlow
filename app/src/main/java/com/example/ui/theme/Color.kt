package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val CategoryStudy = Color(0xFF3B82F6) // Blue
val CategoryProjects = Color(0xFF10B981) // Green
val CategoryPersonal = Color(0xFF8B5CF6) // Purple
val CategoryImportant = Color(0xFFF59E0B) // Orange / Amber
val CategoryDeadline = Color(0xFFEF4444) // Red
val CategoryNeutral = Color(0xFF64748B) // Blue-gray
val CategoryHealth = Color(0xFF06B6D4) // Cyan

fun getCategoryColor(categoryId: String, customColorHex: String? = null): Color {
    if (!customColorHex.isNullOrBlank()) {
        try {
            val hex = customColorHex.removePrefix("#")
            val parsed = hex.toLong(16)
            return if (hex.length == 6) Color(0xFF000000 or parsed) else Color(parsed)
        } catch (_: Exception) {}
    }
    return when (categoryId.lowercase()) {
        "study" -> CategoryStudy
        "projects", "project" -> CategoryProjects
        "personal" -> CategoryPersonal
        "important" -> CategoryImportant
        "deadline" -> CategoryDeadline
        "health", "fitness" -> CategoryHealth
        else -> CategoryNeutral
    }
}
