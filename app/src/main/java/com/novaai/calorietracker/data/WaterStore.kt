package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

data class WaterState(val intakeMl: Int, val history: List<Int>)

/**
 * Single source of truth for the Water Tracker's locally saved intake.
 * Used by both the Water Tracker screen and the Home dashboard tile.
 */
object WaterStore {
    private const val PREFS_NAME = "water_tracker"
    private const val KEY_INTAKE_ML = "intake_ml"
    private const val KEY_ADD_HISTORY = "add_history"
    private const val KEY_INTAKE_DATE = "intake_date"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Applies the day-rollover rule (a previous day's total starts the new
     * day at zero) and returns today's intake and undo history.
     */
    fun loadToday(context: Context): WaterState {
        val p = prefs(context)
        val today = LocalDate.now()
        val savedDate = p.getString(KEY_INTAKE_DATE, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (savedDate == null || savedDate.isBefore(today)) {
            val editor = p.edit()
            if (savedDate != null) {
                // A previous day's total: start the new day at zero.
                editor.putInt(KEY_INTAKE_ML, 0)
                editor.putString(KEY_ADD_HISTORY, "")
            }
            editor.putString(KEY_INTAKE_DATE, today.toString())
            editor.apply()
        }
        return WaterState(
            intakeMl = p.getInt(KEY_INTAKE_ML, 1800),
            history = p.getString(KEY_ADD_HISTORY, "").orEmpty()
                .split(",")
                .mapNotNull { it.toIntOrNull() }
        )
    }

    fun save(context: Context, intakeMl: Int, history: List<Int>) {
        prefs(context).edit()
            .putInt(KEY_INTAKE_ML, intakeMl)
            .putString(KEY_ADD_HISTORY, history.joinToString(","))
            .apply()
    }
}
