package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences

data class WeightGoals(val startKg: Float?, val goalKg: Float?)

/**
 * Single source of truth for the user's locally saved start and goal weight.
 * Used by the Weight Tracker's Goal Settings section.
 */
object WeightGoalStore {
    private const val PREFS_NAME = "weight_goals"
    private const val KEY_START_KG = "start_weight_kg"
    private const val KEY_GOAL_KG = "goal_weight_kg"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns the saved values; a null field means the user never saved one. */
    fun load(context: Context): WeightGoals {
        val p = prefs(context)
        return WeightGoals(
            startKg = p.getFloat(KEY_START_KG, -1f).takeIf { it > 0f },
            goalKg = p.getFloat(KEY_GOAL_KG, -1f).takeIf { it > 0f }
        )
    }

    fun saveStart(context: Context, kg: Float) {
        if (kg <= 0f) return
        prefs(context).edit().putFloat(KEY_START_KG, kg).apply()
    }

    fun saveGoal(context: Context, kg: Float) {
        if (kg <= 0f) return
        prefs(context).edit().putFloat(KEY_GOAL_KG, kg).apply()
    }
}
