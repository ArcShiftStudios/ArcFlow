package com.example.service

import com.example.data.entity.CalendarEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.EventEntity
import com.example.data.entity.TaskEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object IcsHelper {
    private val icsDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")

    fun exportToIcs(events: List<EventEntity>): String {
        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCALENDAR")
        sb.appendLine("VERSION:2.0")
        sb.appendLine("PRODID:-//ArcShift Studios//ArcFlow 1.0//EN")
        sb.appendLine("CALSCALE:GREGORIAN")

        for (event in events) {
            val date = LocalDate.ofEpochDay(event.dateEpochDay)
            val startH = event.startMinuteOfDay / 60
            val startM = event.startMinuteOfDay % 60
            val endH = event.endMinuteOfDay / 60
            val endM = event.endMinuteOfDay % 60

            val startDt = LocalDateTime.of(date, LocalTime.of(startH, startM))
            val endDt = LocalDateTime.of(date, LocalTime.of(endH, endM))

            sb.appendLine("BEGIN:VEVENT")
            sb.appendLine("UID:arc-${event.id}-${event.createdAt}@arcshift.studios")
            sb.appendLine("SUMMARY:${escapeIcs(event.title)}")
            if (event.description.isNotBlank()) {
                sb.appendLine("DESCRIPTION:${escapeIcs(event.description)}")
            }
            if (event.location.isNotBlank()) {
                sb.appendLine("LOCATION:${escapeIcs(event.location)}")
            }
            sb.appendLine("DTSTART:${startDt.format(icsDateFormatter)}")
            sb.appendLine("DTEND:${endDt.format(icsDateFormatter)}")
            sb.appendLine("CATEGORIES:${event.categoryId.uppercase()}")
            if (event.recurrence != "NONE") {
                val rrule = when (event.recurrence) {
                    "DAILY" -> "RRULE:FREQ=DAILY"
                    "WEEKLY" -> "RRULE:FREQ=WEEKLY"
                    "MONTHLY" -> "RRULE:FREQ=MONTHLY"
                    "WEEKDAYS" -> "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"
                    else -> null
                }
                if (rrule != null) sb.appendLine(rrule)
            }
            sb.appendLine("END:VEVENT")
        }

        sb.appendLine("END:VCALENDAR")
        return sb.toString()
    }

    fun parseIcs(icsContent: String): List<EventEntity> {
        val result = mutableListOf<EventEntity>()
        val lines = icsContent.lines()
        var inEvent = false
        var summary = "Imported Event"
        var description = ""
        var location = ""
        var category = "personal"
        var dateEpochDay = LocalDate.now().toEpochDay()
        var startMin = 9 * 60
        var endMin = 10 * 60
        var recurrence = "NONE"

        for (rawLine in lines) {
            val line = rawLine.trim()
            when {
                line == "BEGIN:VEVENT" -> {
                    inEvent = true
                    summary = "Imported Event"
                    description = ""
                    location = ""
                    category = "personal"
                    dateEpochDay = LocalDate.now().toEpochDay()
                    startMin = 9 * 60
                    endMin = 10 * 60
                    recurrence = "NONE"
                }
                line == "END:VEVENT" -> {
                    inEvent = false
                    result.add(
                        EventEntity(
                            title = summary,
                            description = description,
                            categoryId = category,
                            dateEpochDay = dateEpochDay,
                            startMinuteOfDay = startMin,
                            endMinuteOfDay = endMin,
                            recurrence = recurrence,
                            location = location
                        )
                    )
                }
                inEvent -> {
                    if (line.startsWith("SUMMARY:")) {
                        summary = unescapeIcs(line.substringAfter("SUMMARY:"))
                    } else if (line.startsWith("DESCRIPTION:")) {
                        description = unescapeIcs(line.substringAfter("DESCRIPTION:"))
                    } else if (line.startsWith("LOCATION:")) {
                        location = unescapeIcs(line.substringAfter("LOCATION:"))
                    } else if (line.startsWith("CATEGORIES:")) {
                        val cat = line.substringAfter("CATEGORIES:").lowercase()
                        category = if (cat.contains("study") || cat.contains("school")) "study"
                        else if (cat.contains("project") || cat.contains("work")) "projects"
                        else if (cat.contains("health")) "health"
                        else if (cat.contains("important")) "important"
                        else "personal"
                    } else if (line.startsWith("DTSTART:")) {
                        val dtStr = line.substringAfter("DTSTART:").replace("Z", "")
                        try {
                            val parsed = LocalDateTime.parse(dtStr, icsDateFormatter)
                            dateEpochDay = parsed.toLocalDate().toEpochDay()
                            startMin = parsed.hour * 60 + parsed.minute
                        } catch (_: Exception) {}
                    } else if (line.startsWith("DTEND:")) {
                        val dtStr = line.substringAfter("DTEND:").replace("Z", "")
                        try {
                            val parsed = LocalDateTime.parse(dtStr, icsDateFormatter)
                            endMin = parsed.hour * 60 + parsed.minute
                        } catch (_: Exception) {}
                    } else if (line.startsWith("RRULE:")) {
                        val rrule = line.uppercase()
                        recurrence = if (rrule.contains("DAILY")) "DAILY"
                        else if (rrule.contains("WEEKLY")) {
                            if (rrule.contains("BYDAY=MO,TU,WE,TH,FR")) "WEEKDAYS" else "WEEKLY"
                        } else if (rrule.contains("MONTHLY")) "MONTHLY"
                        else "NONE"
                    }
                }
            }
        }
        return result
    }

    private fun escapeIcs(text: String): String =
        text.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n")

    private fun unescapeIcs(text: String): String =
        text.replace("\\n", "\n").replace("\\,", ",").replace("\\;", ";").replace("\\\\", "\\")
}
