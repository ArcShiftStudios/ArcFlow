package com.example

import com.example.data.entity.EventEntity
import com.example.service.EventParser
import com.example.service.IcsHelper
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {
    @Test
    fun testEventParser_quickAdd() {
        val parsed = EventParser.parse("Physics tomorrow 5pm for 1 hour")
        assertEquals("Physics", parsed.title)
        assertEquals("study", parsed.categoryId)
        assertEquals(17 * 60, parsed.startMinuteOfDay) // 5pm = 17:00
        assertEquals(18 * 60, parsed.endMinuteOfDay) // 18:00
        assertEquals(LocalDate.now().plusDays(1).toEpochDay(), parsed.dateEpochDay)
    }

    @Test
    fun testEventParser_recurrenceAndReminder() {
        val parsed = EventParser.parse("Math every Monday at 6pm remind 30 min before")
        assertEquals("Math", parsed.title)
        assertEquals("WEEKLY", parsed.recurrence)
        assertEquals(18 * 60, parsed.startMinuteOfDay)
        assertEquals(30, parsed.reminderMinutesBefore)
    }

    @Test
    fun testIcsHelper_exportAndImport() {
        val original = listOf(
            EventEntity(
                id = 10,
                title = "Calculus Exam",
                description = "Chapter 4-8",
                categoryId = "study",
                dateEpochDay = LocalDate.now().toEpochDay(),
                startMinuteOfDay = 9 * 60,
                endMinuteOfDay = 11 * 60,
                location = "Hall A"
            )
        )
        val ics = IcsHelper.exportToIcs(original)
        assertTrue(ics.contains("SUMMARY:Calculus Exam"))
        assertTrue(ics.contains("LOCATION:Hall A"))

        val parsedBack = IcsHelper.parseIcs(ics)
        assertEquals(1, parsedBack.size)
        assertEquals("Calculus Exam", parsedBack[0].title)
        assertEquals("Hall A", parsedBack[0].location)
    }
}
