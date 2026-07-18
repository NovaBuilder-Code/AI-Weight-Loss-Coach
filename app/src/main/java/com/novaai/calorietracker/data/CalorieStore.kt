package com.novaai.calorietracker.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class StoredMeal(val name: String, val kcal: Int, val date: String)

/**
 * Single source of truth for the Calorie Tracker's locally saved meals.
 * Used by both the Calorie Tracker screen and the Home dashboard card.
 */
object CalorieStore {
    private const val PREFS_NAME = "calorie_tracker"
    private const val KEY_MEALS = "meals"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Loads saved meals, keeping only today's. Meals from previous days are
     * dropped and pruned from storage (the daily reset).
     */
    fun loadTodayMeals(context: Context): List<StoredMeal> {
        val all = runCatching {
            val array = JSONArray(prefs(context).getString(KEY_MEALS, "[]").orEmpty())
            (0 until array.length()).mapNotNull { i ->
                val obj = array.optJSONObject(i) ?: return@mapNotNull null
                val name = obj.optString("name")
                val kcal = obj.optInt("kcal", -1)
                if (name.isBlank() || kcal < 0) null
                else StoredMeal(name, kcal, obj.optString("date"))
            }
        }.getOrDefault(emptyList())
        val today = LocalDate.now().toString()
        val todays = all.filter { it.date == today }
        if (todays.size != all.size) saveMeals(context, todays)
        return todays
    }

    fun saveMeals(context: Context, meals: List<StoredMeal>) {
        val array = JSONArray()
        meals.forEach { meal ->
            array.put(
                JSONObject()
                    .put("name", meal.name)
                    .put("kcal", meal.kcal)
                    .put("date", meal.date)
            )
        }
        prefs(context).edit().putString(KEY_MEALS, array.toString()).apply()
    }
}
