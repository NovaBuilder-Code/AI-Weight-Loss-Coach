package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

/**
 * Persists which reminders have already fired so the recurring workers never
 * post duplicates within the same window/day.
 */
object ReminderDedupStore {
    private const val PREFS_NAME = "reminder_dedup"
    private const val KEY_MEAL_BREAKFAST = "meal_breakfast_date"
    private const val KEY_MEAL_LUNCH = "meal_lunch_date"
    private const val KEY_MEAL_DINNER = "meal_dinner_date"
    private const val KEY_MOTIVATION = "motivation_date"
    private const val KEY_WEIGH_IN = "weigh_in_date"
    private const val KEY_HYDRATION_LAST_MS = "hydration_last_ms"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun date(key: String, p: SharedPreferences): LocalDate? =
        p.getString(key, null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    fun lastMealDate(context: Context, meal: ReminderSchedule.Meal): LocalDate? =
        date(keyForMeal(meal), prefs(context))

    fun markMealNotified(context: Context, meal: ReminderSchedule.Meal, date: LocalDate) {
        prefs(context).edit().putString(keyForMeal(meal), date.toString()).apply()
    }

    private fun keyForMeal(meal: ReminderSchedule.Meal): String = when (meal) {
        ReminderSchedule.Meal.BREAKFAST -> KEY_MEAL_BREAKFAST
        ReminderSchedule.Meal.LUNCH -> KEY_MEAL_LUNCH
        ReminderSchedule.Meal.DINNER -> KEY_MEAL_DINNER
    }

    fun lastMotivationDate(context: Context): LocalDate? =
        date(KEY_MOTIVATION, prefs(context))

    fun markMotivationNotified(context: Context, date: LocalDate) {
        prefs(context).edit().putString(KEY_MOTIVATION, date.toString()).apply()
    }

    fun lastWeighInDate(context: Context): LocalDate? =
        date(KEY_WEIGH_IN, prefs(context))

    fun markWeighInNotified(context: Context, date: LocalDate) {
        prefs(context).edit().putString(KEY_WEIGH_IN, date.toString()).apply()
    }

    fun lastHydrationMs(context: Context): Long? =
        prefs(context).getLong(KEY_HYDRATION_LAST_MS, -1L).takeIf { it >= 0 }

    fun markHydrationNotified(context: Context, timeMs: Long) {
        prefs(context).edit().putLong(KEY_HYDRATION_LAST_MS, timeMs).apply()
    }
}
