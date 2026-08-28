package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

data class StreakState(
    val currentStreak: Int,
    val bestStreak: Int,
    val lastCompleted: LocalDate?,
    val todayCompleted: Boolean,
    val completedDates: Set<LocalDate>
)

/**
 * Single source of truth for the Daily Streaks' locally saved streak.
 * Used by the Daily Streaks screen. Survives app restarts via
 * SharedPreferences and applies the missed-day reset when a new day starts.
 * Completed calendar dates are kept even when the streak resets so the
 * week strip can still show this week's checkmarks.
 */
object StreakStore {
    private const val PREFS_NAME = "daily_streaks"
    private const val KEY_STREAK = "current_streak"
    private const val KEY_BEST_STREAK = "best_streak"
    private const val KEY_LAST_COMPLETED = "last_completed_date"
    private const val KEY_COMPLETED_DATES = "completed_dates"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun readCompletedDates(p: SharedPreferences, lastCompleted: LocalDate?): Set<LocalDate> {
        val stored = StreakLogic.parseCompletedDates(p.getString(KEY_COMPLETED_DATES, null))
        return if (lastCompleted != null) stored + lastCompleted else stored
    }

    /**
     * Loads the saved streak, resetting it to zero if a required day was
     * missed (the streak does not carry over into the new day). This week's
     * completed dates are left intact.
     */
    fun load(context: Context): StreakState {
        val p = prefs(context)
        val today = LocalDate.now()
        val lastCompleted = p.getString(KEY_LAST_COMPLETED, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val streak = p.getInt(KEY_STREAK, 0)
        val best = p.getInt(KEY_BEST_STREAK, 0)
        val completedDates = readCompletedDates(p, lastCompleted)

        val active = StreakLogic.activeStreak(lastCompleted, streak, today)
        val editor = p.edit()
        var dirty = false
        if (active != streak) {
            editor.putInt(KEY_STREAK, active)
            dirty = true
        }
        val serialized = StreakLogic.serializeCompletedDates(completedDates)
        if (serialized != (p.getString(KEY_COMPLETED_DATES, null) ?: "")) {
            editor.putString(KEY_COMPLETED_DATES, serialized)
            dirty = true
        }
        if (dirty) editor.apply()

        return StreakState(
            currentStreak = active,
            bestStreak = best,
            lastCompleted = lastCompleted,
            todayCompleted = lastCompleted == today || today in completedDates,
            completedDates = completedDates
        )
    }

    /** Marks the daily activity complete for today and persists the new streak. */
    fun complete(context: Context): StreakState {
        val state = load(context)
        val today = LocalDate.now()
        val newStreak = StreakLogic.afterCompletion(state.lastCompleted, state.currentStreak, today)
        val newBest = StreakLogic.updatedBest(state.bestStreak, newStreak)
        val completedDates = StreakLogic.withCompletedDay(state.completedDates, today)
        prefs(context).edit()
            .putInt(KEY_STREAK, newStreak)
            .putInt(KEY_BEST_STREAK, newBest)
            .putString(KEY_LAST_COMPLETED, today.toString())
            .putString(KEY_COMPLETED_DATES, StreakLogic.serializeCompletedDates(completedDates))
            .apply()
        return StreakState(
            currentStreak = newStreak,
            bestStreak = newBest,
            lastCompleted = today,
            todayCompleted = true,
            completedDates = completedDates
        )
    }
}
