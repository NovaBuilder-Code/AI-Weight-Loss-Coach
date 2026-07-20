package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

/**
 * Single source of truth for the Steps Tracker's locally saved step count.
 * Used by the Steps (Walking) Tracker screen.
 */
object StepsStore {
    private const val PREFS_NAME = "steps_tracker"
    private const val KEY_STEPS = "today_steps"
    private const val KEY_STEPS_DATE = "steps_date"
    private const val DEFAULT_STEPS = 0

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Applies the day-rollover rule (a previous day's count starts the new
     * day at zero) and returns today's step count.
     */
    fun loadToday(context: Context): Int {
        val p = prefs(context)
        val today = LocalDate.now()
        val savedDate = p.getString(KEY_STEPS_DATE, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (savedDate != null && savedDate.isBefore(today)) {
            // A previous day's count: start the new day at zero.
            p.edit()
                .putInt(KEY_STEPS, 0)
                .putString(KEY_STEPS_DATE, today.toString())
                .apply()
        }
        return p.getInt(KEY_STEPS, DEFAULT_STEPS)
    }

    fun save(context: Context, steps: Int) {
        prefs(context).edit()
            .putInt(KEY_STEPS, steps)
            .putString(KEY_STEPS_DATE, LocalDate.now().toString())
            .apply()
    }
}
