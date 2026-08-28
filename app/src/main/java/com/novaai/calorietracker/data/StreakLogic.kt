package com.novaai.calorietracker.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Pure, Android-free streak decisions for the Daily Streaks feature. Kept in
 * the data layer so the increment/reset rules are unit-testable without a
 * device. The streak counts consecutive calendar days on which the user
 * completed the required daily activity (the streaks "Complete" button).
 */
object StreakLogic {

    /**
     * The active streak as of [today]: kept as-is while the user is up to
     * date (completed today or yesterday), zero once a required day is missed.
     */
    fun activeStreak(lastCompleted: LocalDate?, streak: Int, today: LocalDate): Int {
        if (lastCompleted == null) return 0
        val days = ChronoUnit.DAYS.between(lastCompleted, today)
        return if (days <= 1) streak else 0
    }

    /**
     * The new streak after the user completes the daily activity on [today].
     * - Already completed today -> unchanged (never double-increments).
     * - Completed yesterday -> streak grows by one.
     * - First completion, or a missed day -> streak restarts at one.
     */
    fun afterCompletion(lastCompleted: LocalDate?, streak: Int, today: LocalDate): Int =
        when {
            lastCompleted == today -> streak
            lastCompleted == today.minusDays(1) -> streak + 1
            else -> 1
        }

    /** Best streak is the max of the previous best and the new current streak. */
    fun updatedBest(best: Int, newStreak: Int): Int = maxOf(best, newStreak)

    /** ISO Monday that starts the week containing [date]. */
    fun mondayOf(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun isDateCompleted(date: LocalDate, completedDates: Set<LocalDate>): Boolean =
        date in completedDates

    /**
     * Seven Mon..Sun flags for the week containing [date], derived from the
     * persisted completed-date set (not reconstructed from the current streak).
     */
    fun weekCompletionFlags(date: LocalDate, completedDates: Set<LocalDate>): List<Boolean> {
        val monday = mondayOf(date)
        return (0L..6L).map { monday.plusDays(it) in completedDates }
    }

    fun parseCompletedDates(raw: String?): Set<LocalDate> {
        if (raw.isNullOrBlank()) return emptySet()
        return raw.split(',')
            .mapNotNull { token ->
                val trimmed = token.trim()
                if (trimmed.isEmpty()) null
                else runCatching { LocalDate.parse(trimmed) }.getOrNull()
            }
            .toSet()
    }

    fun serializeCompletedDates(dates: Set<LocalDate>): String =
        dates.sorted().joinToString(",") { it.toString() }

    fun withCompletedDay(existing: Set<LocalDate>, day: LocalDate): Set<LocalDate> =
        existing + day
}
