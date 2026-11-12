package com.example

import com.example.data.model.ScheduleCategory
import com.example.parser.NaturalLanguageParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ExampleUnitTest {

    private val baseDate = LocalDate.of(2026, 11, 11) // Wednesday

    @Test
    fun testParseBesokWithHour() {
        val input = "Kerja kelompok besok jam 19.00"
        val result = NaturalLanguageParser.parse(input, baseDate)

        assertEquals("Kerja kelompok", result.title)
        assertEquals(baseDate.plusDays(1), result.date)
        assertEquals(LocalTime.of(19, 0), result.startTime)
        assertEquals(ScheduleCategory.MEETING, result.category)
    }

    @Test
    fun testParseWithReminder() {
        val input = "Rapat tim besok jam 9 pagi, ingatkan 15 menit sebelumnya"
        val result = NaturalLanguageParser.parse(input, baseDate)

        assertEquals("Rapat tim", result.title)
        assertEquals(baseDate.plusDays(1), result.date)
        assertEquals(LocalTime.of(9, 0), result.startTime)
        assertTrue(result.isReminderEnabled)
        assertEquals(15, result.reminderMinutesBefore)
        assertEquals(ScheduleCategory.MEETING, result.category)
    }

    @Test
    fun testParseLusaAndPersonal() {
        val input = "Makan siang lusa jam 12:30"
        val result = NaturalLanguageParser.parse(input, baseDate)

        assertEquals("Makan siang", result.title)
        assertEquals(baseDate.plusDays(2), result.date)
        assertEquals(LocalTime.of(12, 30), result.startTime)
        assertEquals(ScheduleCategory.PERSONAL, result.category)
    }

    @Test
    fun testParseRoutineMorning() {
        val input = "Olahraga pagi jam 06.00"
        val result = NaturalLanguageParser.parse(input, baseDate)

        assertEquals("Olahraga pagi", result.title)
        assertEquals(baseDate, result.date)
        assertEquals(LocalTime.of(6, 0), result.startTime)
        assertEquals(ScheduleCategory.ROUTINE, result.category)
    }
}
