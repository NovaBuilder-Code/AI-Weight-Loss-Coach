package com.novaai.calorietracker.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pure, Android-free scheduling decisions for Task #14B reminders. Kept in the
 * data layer so the time windows / dedup rules are unit-testable without a
 * device. Workers call these and only these to decide whether to post.
 */
object ReminderSchedule {

    enum class Meal { BREAKFAST, LUNCH, DINNER }

    /**
     * Meal windows: 07:00–08:59, 12:00–13:59, 18:00–19:59.
     * Returns the meal due at the given hour, or null outside those windows.
     */
    fun mealForTime(hour: Int): Meal? = when (hour) {
        in 7..8 -> Meal.BREAKFAST
        in 12..13 -> Meal.LUNCH
        in 18..19 -> Meal.DINNER
        else -> null
    }

    /** Daytime only (08:00–21:59) — hydration never fires during sleep/night hours. */
    fun isHydrationWindow(hour: Int): Boolean = hour in 8..21

    /** One morning motivation slot (07:00–08:59). */
    fun isMotivationHour(hour: Int): Boolean = hour in 7..8

    /** A weekly weigh-in is due when there is no prior one or it is >= 7 days ago. */
    fun isWeighInDue(lastNotified: LocalDate?, today: LocalDate): Boolean =
        lastNotified == null || ChronoUnit.DAYS.between(lastNotified, today) >= 7

    /** A meal reminder is due only if it has not already fired today. */
    fun shouldSendMeal(lastNotified: LocalDate?, today: LocalDate): Boolean =
        lastNotified != today

    /** Motivation is due at most once per calendar day. */
    fun shouldSendMotivation(lastNotified: LocalDate?, today: LocalDate): Boolean =
        lastNotified != today

    /** Hydration is due only when it has been at least [intervalMinutes] since the last one. */
    fun shouldSendHydration(lastSentMs: Long?, nowMs: Long, intervalMinutes: Long): Boolean =
        lastSentMs == null || (nowMs - lastSentMs) >= intervalMinutes * 60_000L
}
