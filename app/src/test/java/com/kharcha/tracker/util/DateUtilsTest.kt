package com.kharcha.tracker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

class DateUtilsTest {

    @Test
    fun getStartOfDayTest() {
        val now = System.currentTimeMillis()
        val startOfDay = DateUtils.getStartOfDay(now)
        val expected = java.time.Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, startOfDay)
    }

    @Test
    fun getEndOfDayTest() {
        val now = System.currentTimeMillis()
        val endOfDay = DateUtils.getEndOfDay(now)
        val expected = java.time.Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, endOfDay)
    }

    @Test
    fun getStartOfWeekTest() {
        val startOfWeek = DateUtils.getStartOfWeek()
        val expected = java.time.LocalDate.now(ZoneId.systemDefault()).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, startOfWeek)
    }

    @Test
    fun getFinancialYearLabelTest() {
        val label = DateUtils.getFinancialYearLabel()
        assertTrue(label.startsWith("FY "))
    }
}
