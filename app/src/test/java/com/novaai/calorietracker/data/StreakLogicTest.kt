package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreakLogicTest {

    private val today = LocalDate.of(2026, 8, 26)

    @Test
    fun consecutiveDayCompletionIncreasesStreak() {
        assertEquals(2, StreakLogic.afterCompletion(today.minusDays(1), 1, today))
        assertEquals(13, StreakLogic.afterCompletion(today.minusDays(1), 12, today))
    }

    @Test
    fun sameDayCompletionDoesNotDoubleIncrement() {
        assertEquals(5, StreakLogic.afterCompletion(today, 5, today))
        assertEquals(12, StreakLogic.afterCompletion(today, 12, today))
    }

    @Test
    fun firstCompletionStartsStreakAtOne() {
        assertEquals(1, StreakLogic.afterCompletion(null, 0, today))
    }

    @Test
    fun missedDayRestartsStreakAtOne() {
        assertEquals(1, StreakLogic.afterCompletion(today.minusDays(2), 7, today))
        assertEquals(1, StreakLogic.afterCompletion(today.minusDays(5), 12, today))
    }

    @Test
    fun missedDayResetsActiveStreakToZero() {
        assertEquals(0, StreakLogic.activeStreak(null, 0, today))
        assertEquals(0, StreakLogic.activeStreak(today.minusDays(2), 7, today))
        assertEquals(0, StreakLogic.activeStreak(today.minusDays(10), 12, today))
    }

    @Test
    fun activeStreakSurvivesTodayAndYesterday() {
        assertEquals(7, StreakLogic.activeStreak(today, 7, today))
        assertEquals(7, StreakLogic.activeStreak(today.minusDays(1), 7, today))
    }

    @Test
    fun bestStreakIsMaxOfPreviousAndNew() {
        assertEquals(12, StreakLogic.updatedBest(12, 5))
        assertEquals(13, StreakLogic.updatedBest(12, 13))
        assertEquals(1, StreakLogic.updatedBest(0, 1))
        assertEquals(7, StreakLogic.updatedBest(7, 7))
    }

    @Test
    fun mondayOfWeekIsIsoMonday() {
        val monday = LocalDate.of(2026, 8, 24)
        assertEquals(monday, StreakLogic.mondayOf(monday))
        assertEquals(monday, StreakLogic.mondayOf(LocalDate.of(2026, 8, 26)))
        assertEquals(monday, StreakLogic.mondayOf(LocalDate.of(2026, 8, 28)))
        assertEquals(monday, StreakLogic.mondayOf(LocalDate.of(2026, 8, 30)))
        assertEquals(LocalDate.of(2026, 8, 31), StreakLogic.mondayOf(LocalDate.of(2026, 9, 1)))
    }

    @Test
    fun weekStripUsesPersistedDatesNotStreak() {
        val monday = LocalDate.of(2026, 8, 24)
        val completed = setOf(
            LocalDate.of(2026, 8, 24),
            LocalDate.of(2026, 8, 25),
            LocalDate.of(2026, 8, 26)
        )
        assertEquals(
            listOf(true, true, true, false, false, false, false),
            StreakLogic.weekCompletionFlags(monday, completed)
        )
        assertEquals(
            listOf(true, true, true, false, false, false, false),
            StreakLogic.weekCompletionFlags(LocalDate.of(2026, 8, 27), completed)
        )
    }

    @Test
    fun weekStripCompletingFridayDoesNotFillThursday() {
        val friday = LocalDate.of(2026, 8, 28)
        val completed = setOf(
            LocalDate.of(2026, 8, 24),
            LocalDate.of(2026, 8, 25),
            LocalDate.of(2026, 8, 26),
            friday
        )
        assertEquals(
            listOf(true, true, true, false, true, false, false),
            StreakLogic.weekCompletionFlags(friday, completed)
        )
        assertTrue(StreakLogic.isDateCompleted(friday, completed))
        assertFalse(StreakLogic.isDateCompleted(LocalDate.of(2026, 8, 27), completed))
    }

    @Test
    fun weekStripSurvivesMissedDayStreakReset() {
        // Miss is visible the day after the gap: completed Mon-Wed, skipped Thu,
        // so on Friday the current streak is 0 but Mon-Wed stay marked.
        val friday = LocalDate.of(2026, 8, 28)
        val lastCompleted = LocalDate.of(2026, 8, 26)
        val completed = setOf(
            LocalDate.of(2026, 8, 24),
            LocalDate.of(2026, 8, 25),
            LocalDate.of(2026, 8, 26)
        )
        assertEquals(0, StreakLogic.activeStreak(lastCompleted, 3, friday))
        assertEquals(
            listOf(true, true, true, false, false, false, false),
            StreakLogic.weekCompletionFlags(friday, completed)
        )
    }

    @Test
    fun parseAndSerializeCompletedDates() {
        val dates = setOf(
            LocalDate.of(2026, 8, 26),
            LocalDate.of(2026, 8, 24)
        )
        val raw = StreakLogic.serializeCompletedDates(dates)
        assertEquals("2026-08-24,2026-08-26", raw)
        assertEquals(dates, StreakLogic.parseCompletedDates(raw))
        assertEquals(emptySet<LocalDate>(), StreakLogic.parseCompletedDates(null))
        assertEquals(emptySet<LocalDate>(), StreakLogic.parseCompletedDates(""))
        assertEquals(
            setOf(LocalDate.of(2026, 8, 24)),
            StreakLogic.parseCompletedDates("2026-08-24,not-a-date")
        )
    }

    @Test
    fun addingTodayToCompletedDatesIsIdempotent() {
        val today = LocalDate.of(2026, 8, 28)
        val once = StreakLogic.withCompletedDay(emptySet(), today)
        val twice = StreakLogic.withCompletedDay(once, today)
        assertEquals(setOf(today), once)
        assertEquals(once, twice)
    }
}
