package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

data class WeightState(val kg: Float, val date: LocalDate)

/**
 * Single source of truth for the user's locally saved current weight.
 * Used by the Weight Tracker screen.
 */
object WeightStore {
    private const val PREFS_NAME = "weight_tracker"
    private const val KEY_WEIGHT_KG = "current_weight_kg"
    private const val KEY_WEIGHT_DATE = "current_weight_date"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns the last weight the user logged, or null if none has been saved yet. */
    fun load(context: Context): WeightState? {
        val p = prefs(context)
        val kg = p.getFloat(KEY_WEIGHT_KG, -1f)
        if (kg <= 0f) return null
        val date = p.getString(KEY_WEIGHT_DATE, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: return null
        return WeightState(kg, date)
    }

    fun save(context: Context, kg: Float) {
        if (kg <= 0f) return
        prefs(context).edit()
            .putFloat(KEY_WEIGHT_KG, kg)
            .putString(KEY_WEIGHT_DATE, LocalDate.now().toString())
            .apply()
    }
}
