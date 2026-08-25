package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Single source of truth for the user's daily sleep-goal (hours) set on the
 * Profile → Goals screen. New users default to 0 hours.
 */
object SleepGoalStore {
    private const val PREFS_NAME = "sleep_goal"
    private const val KEY_HOURS = "hours"
    private const val DEFAULT_HOURS = 0f

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(context: Context): Float = prefs(context).getFloat(KEY_HOURS, DEFAULT_HOURS)

    fun save(context: Context, hours: Float) {
        prefs(context).edit().putFloat(KEY_HOURS, hours).apply()
    }
}
