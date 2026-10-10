package com.example.service

import com.example.data.entity.EventEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.util.Locale
import java.util.regex.Pattern

data class ParsedEventProposal(
    val title: String,
    val description: String = "",
    val categoryId: String = "study",
    val dateEpochDay: Long,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val recurrence: String = "NONE",
    val reminderMinutesBefore: Int = 15,
    val location: String = ""
)

object EventParser {
    fun parse(input: String): ParsedEventProposal {
        val trimmed = input.trim()
        val lower = trimmed.lowercase(Locale.ROOT)
        val today = LocalDate.now()
        var targetDate = today
        var startMinute = 15 * 60 // default 3pm if not specified
        var endMinute = 16 * 60 // default 1 hour
        var categoryId = "study"
        var recurrence = "NONE"
        var reminderMinutes = 15
        var location = ""
        var rawTitle = trimmed

        // Check date keywords
        if (lower.contains("tomorrow")) {
            targetDate = today.plusDays(1)
            rawTitle = rawTitle.replace(Regex("(?i)\\btomorrow\\b"), "")
        } else if (lower.contains("today")) {
            targetDate = today
            rawTitle = rawTitle.replace(Regex("(?i)\\btoday\\b"), "")
        } else if (lower.contains("day after tomorrow")) {
            targetDate = today.plusDays(2)
            rawTitle = rawTitle.replace(Regex("(?i)\\bday after tomorrow\\b"), "")
        }

        // Check recurrence & weekdays: "every monday", "every day", "on wednesday"
        val daysOfWeek = mapOf(
            "monday" to DayOfWeek.MONDAY,
            "tuesday" to DayOfWeek.TUESDAY,
            "wednesday" to DayOfWeek.WEDNESDAY,
            "thursday" to DayOfWeek.THURSDAY,
            "friday" to DayOfWeek.FRIDAY,
            "saturday" to DayOfWeek.SATURDAY,
            "sunday" to DayOfWeek.SUNDAY
        )

        for ((dayName, dayEnum) in daysOfWeek) {
            if (lower.contains("every $dayName")) {
                recurrence = "WEEKLY"
                var d = today
                while (d.dayOfWeek != dayEnum) {
                    d = d.plusDays(1)
                }
                targetDate = d
                rawTitle = rawTitle.replace(Regex("(?i)\\bevery $dayName\\b"), "")
                break
            } else if (lower.contains(dayName)) {
                var d = today
                while (d.dayOfWeek != dayEnum) {
                    d = d.plusDays(1)
                }
                if (d == today && LocalTime.now().hour > 18) d = d.plusWeeks(1)
                targetDate = d
                rawTitle = rawTitle.replace(Regex("(?i)\\b(on )?$dayName\\b"), "")
                break
            }
        }

        if (lower.contains("every day") || lower.contains("daily")) {
            recurrence = "DAILY"
            rawTitle = rawTitle.replace(Regex("(?i)\\b(every day|daily)\\b"), "")
        } else if (lower.contains("weekdays")) {
            recurrence = "WEEKDAYS"
            rawTitle = rawTitle.replace(Regex("(?i)\\bweekdays\\b"), "")
        }

        // Check explicit date like "October 12" or "12 Oct" or "12/10"
        val monthNames = Month.values()
        for (m in monthNames) {
            val mName = m.name.lowercase(Locale.ROOT)
            val mShort = mName.take(3)
            val regex = Regex("(?i)\\b($mName|$mShort)\\s+(\\d{1,2})\\b")
            val match = regex.find(rawTitle)
            if (match != null) {
                val dayNum = match.groupValues[2].toIntOrNull() ?: 1
                try {
                    val candidate = LocalDate.of(today.year, m, dayNum.coerceIn(1, m.length(false)))
                    targetDate = if (candidate.isBefore(today)) candidate.plusYears(1) else candidate
                } catch (_: Exception) {}
                rawTitle = rawTitle.replace(match.value, "")
                break
            }
        }

        // Check reminders like "remind me 30 min before" or "remind 15m"
        val reminderRegex = Regex("(?i)remind(?: me)? (\\d+)\\s*(min|mins|minutes|m|hour|hours|h)?(?: before)?")
        val reminderMatch = reminderRegex.find(rawTitle)
        if (reminderMatch != null) {
            val num = reminderMatch.groupValues[1].toIntOrNull() ?: 15
            val unit = reminderMatch.groupValues[2].lowercase(Locale.ROOT)
            reminderMinutes = if (unit.startsWith("h")) num * 60 else num
            rawTitle = rawTitle.replace(reminderMatch.value, "")
        }

        // Check duration like "for 2 hours" or "for 45 min"
        var durationMinutes = 60
        val durationRegex = Regex("(?i)for (\\d+)\\s*(hour|hours|hr|hrs|h|min|mins|minutes|m)\\b")
        val durMatch = durationRegex.find(rawTitle)
        if (durMatch != null) {
            val durVal = durMatch.groupValues[1].toIntOrNull() ?: 1
            val unit = durMatch.groupValues[2].lowercase(Locale.ROOT)
            durationMinutes = if (unit.startsWith("h")) durVal * 60 else durVal
            rawTitle = rawTitle.replace(durMatch.value, "")
        }

        // Check time like "at 5pm", "5:30pm", "16:00", "9 AM", "14:00 to 15:30"
        val timeRangeRegex = Regex("(?i)\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?\\s*(?:to|-)\\s*(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?\\b")
        val rangeMatch = timeRangeRegex.find(rawTitle)
        if (rangeMatch != null) {
            var startH = rangeMatch.groupValues[1].toIntOrNull() ?: 9
            val startM = rangeMatch.groupValues[2].toIntOrNull() ?: 0
            val startAmpm = rangeMatch.groupValues[3].lowercase(Locale.ROOT)
            var endH = rangeMatch.groupValues[4].toIntOrNull() ?: (startH + 1)
            val endM = rangeMatch.groupValues[5].toIntOrNull() ?: 0
            val endAmpm = rangeMatch.groupValues[6].lowercase(Locale.ROOT)

            if (startAmpm == "pm" && startH < 12) startH += 12
            if (startAmpm == "am" && startH == 12) startH = 0
            if (endAmpm == "pm" && endH < 12) endH += 12
            if (endAmpm == "am" && endH == 12) endH = 0

            startMinute = startH * 60 + startM
            endMinute = endH * 60 + endM
            rawTitle = rawTitle.replace(rangeMatch.value, "")
        } else {
            val singleTimeRegex = Regex("(?i)(?:at\\s+)?\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b")
            val singleMatch = singleTimeRegex.find(rawTitle)
            if (singleMatch != null) {
                var h = singleMatch.groupValues[1].toIntOrNull() ?: 9
                val m = singleMatch.groupValues[2].toIntOrNull() ?: 0
                val ampm = singleMatch.groupValues[3].lowercase(Locale.ROOT)
                if (ampm == "pm" && h < 12) h += 12
                if (ampm == "am" && h == 12) h = 0
                startMinute = h * 60 + m
                endMinute = startMinute + durationMinutes
                rawTitle = rawTitle.replace(singleMatch.value, "")
            }
        }

        // Detect Category by keywords
        val titleLower = lower
        if (titleLower.contains("exam") || titleLower.contains("deadline") || titleLower.contains("due") || titleLower.contains("test")) {
            categoryId = "deadline"
        } else if (titleLower.contains("physics") || titleLower.contains("math") || titleLower.contains("study") || titleLower.contains("tuition") || titleLower.contains("lecture") || titleLower.contains("homework") || titleLower.contains("class")) {
            categoryId = "study"
        } else if (titleLower.contains("project") || titleLower.contains("sprint") || titleLower.contains("code") || titleLower.contains("design") || titleLower.contains("meeting") || titleLower.contains("review")) {
            categoryId = "projects"
        } else if (titleLower.contains("workout") || titleLower.contains("gym") || titleLower.contains("run") || titleLower.contains("health") || titleLower.contains("meditation") || titleLower.contains("yoga")) {
            categoryId = "health"
        } else if (titleLower.contains("important") || titleLower.contains("urgent") || titleLower.contains("priority")) {
            categoryId = "important"
        } else if (titleLower.contains("dinner") || titleLower.contains("lunch") || titleLower.contains("birthday") || titleLower.contains("party") || titleLower.contains("family")) {
            categoryId = "personal"
        } else if (titleLower.contains("free time") || titleLower.contains("coffee") || titleLower.contains("break") || titleLower.contains("relax")) {
            categoryId = "neutral"
        }

        // Clean up title text
        val cleanedTitle = rawTitle
            .replace(Regex("(?i)\\b(at|on|for|in|remind|me|every)\\b"), " ")
            .replace(Regex("[,:;\\-\\+]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifEmpty { "New Event" }
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

        return ParsedEventProposal(
            title = cleanedTitle,
            description = "Created via Arc Quick Add",
            categoryId = categoryId,
            dateEpochDay = targetDate.toEpochDay(),
            startMinuteOfDay = startMinute,
            endMinuteOfDay = endMinute.coerceAtLeast(startMinute + 15),
            recurrence = recurrence,
            reminderMinutesBefore = reminderMinutes,
            location = location
        )
    }
}
