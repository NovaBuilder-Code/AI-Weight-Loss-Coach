package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReminderScheduleTest {

    @Test
    fun mealWindowsAreBreakfastLunchDinner() {
        assertEquals(ReminderSchedule.Meal.BREAKFAST, ReminderSchedule.mealForTime(7))
        assertEquals(ReminderSchedule.Meal.BREAKFAST, ReminderSchedule.mealForTime(8))
        assertEquals(ReminderSchedule.Meal.LUNCH, ReminderSchedule.mealForTime(12))
        assertEquals(ReminderSchedule.Meal.LUNCH, ReminderSchedule.mealForTime(13))
        assertEquals(ReminderSchedule.Meal.DINNER, ReminderSchedule.mealForTime(18))
        assertEquals(ReminderSchedule.Meal.DINNER, ReminderSchedule.mealForTime(19))
        assertNull(ReminderSchedule.mealForTime(6))
        assertNull(ReminderSchedule.mealForTime(9))
        assertNull(ReminderSchedule.mealForTime(14))
        assertNull(ReminderSchedule.mealForTime(20))
        assertNull(ReminderSchedule.mealForTime(23))
    }

    @Test
    fun hydrationOnlyDuringDaytime() {
        assertTrue(ReminderSchedule.isHydrationWindow(8))
        assertTrue(ReminderSchedule.isHydrationWindow(15))
        assertTrue(ReminderSchedule.isHydrationWindow(21))
        assertFalse(ReminderSchedule.isHydrationWindow(7))
        assertFalse(ReminderSchedule.isHydrationWindow(22))
        assertFalse(ReminderSchedule.isHydrationWindow(0))
        assertFalse(ReminderSchedule.isHydrationWindow(23))
    }

    @Test
    fun motivationOnlyInMorning() {
        assertTrue(ReminderSchedule.isMotivationHour(7))
        assertTrue(ReminderSchedule.isMotivationHour(8))
        assertFalse(ReminderSchedule.isMotivationHour(9))
        assertFalse(ReminderSchedule.isMotivationHour(12))
        assertFalse(ReminderSchedule.isMotivationHour(6))
    }

    @Test
    fun weighInIsWeekly() {
        val today = LocalDate.of(2026, 8, 25)
        assertTrue(ReminderSchedule.isWeighInDue(null, today))
        assertTrue(ReminderSchedule.isWeighInDue(today.minusDays(7), today))
        assertTrue(ReminderSchedule.isWeighInDue(today.minusDays(14), today))
        assertFalse(ReminderSchedule.isWeighInDue(today, today))
        assertFalse(ReminderSchedule.isWeighInDue(today.minusDays(6), today))
    }

    @Test
    fun mealAndMotivationFireOncePerDay() {
        val today = LocalDate.of(2026, 8, 25)
        assertTrue(ReminderSchedule.shouldSendMeal(null, today))
        assertTrue(ReminderSchedule.shouldSendMeal(today.minusDays(1), today))
        assertFalse(ReminderSchedule.shouldSendMeal(today, today))
        assertTrue(ReminderSchedule.shouldSendMotivation(null, today))
        assertFalse(ReminderSchedule.shouldSendMotivation(today, today))
    }

    @Test
    fun hydrationThrottledByInterval() {
        val now = 100_000L
        val interval = 180L
        assertTrue(ReminderSchedule.shouldSendHydration(null, now, interval))
        // 2h ago (< 3h) → not due yet.
        assertFalse(ReminderSchedule.shouldSendHydration(now - 120 * 60_000L, now, interval))
        // 3h ago → due.
        assertTrue(ReminderSchedule.shouldSendHydration(now - 180 * 60_000L, now, interval))
        // 4h ago → due.
        assertTrue(ReminderSchedule.shouldSendHydration(now - 240 * 60_000L, now, interval))
    }
}
