package com.novaai.calorietracker.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class StoredMeal(
    val name: String,
    val kcal: Int,
    val date: String,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0
)

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
                else StoredMeal(
                    name = name,
                    kcal = kcal,
                    date = obj.optString("date"),
                    proteinG = obj.optDouble("proteinG", 0.0),
                    carbsG = obj.optDouble("carbsG", 0.0),
                    fatG = obj.optDouble("fatG", 0.0)
                )
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
                    .put("proteinG", meal.proteinG)
                    .put("carbsG", meal.carbsG)
                    .put("fatG", meal.fatG)
            )
        }
        prefs(context).edit().putString(KEY_MEALS, array.toString()).apply()
    }

    /**
     * Builds one [StoredMeal] per detected food so a scan can be added to
     * the calorie log. Uses each food's own calories (not the scan total).
     */
    fun mealsFromScan(
        result: FoodScanResult,
        date: String = LocalDate.now().toString()
    ): List<StoredMeal> = result.foods.map {
        StoredMeal(
            name = it.name,
            kcal = it.calories,
            date = date,
            proteinG = it.proteinG,
            carbsG = it.carbsG,
            fatG = it.fatG
        )
    }

    /**
     * Formats the macro values for a meal row ("40 g protein · 0 g carbs ·
     * 8 g fat"), or an empty string when no macros were recorded.
     */
    fun formatMacros(proteinG: Double, carbsG: Double, fatG: Double): String =
        if (proteinG <= 0.0 && carbsG <= 0.0 && fatG <= 0.0) ""
        else "${formatGrams(proteinG)} g protein · ${formatGrams(carbsG)} g carbs · ${formatGrams(fatG)} g fat"

    private fun formatGrams(v: Double): String =
        if (v == Math.floor(v) && !v.isInfinite()) v.toInt().toString()
        else v.toString().trimEnd('0').trimEnd('.')

    /**
     * Appends [newMeals] to [existing], skipping entries already present
     * (same name, kcal and date) so a repeated tap cannot log duplicates.
     */
    fun appendLoggedFoods(existing: List<StoredMeal>, newMeals: List<StoredMeal>): List<StoredMeal> {
        val existingKeys = existing.map { it.name to it.kcal to it.date }.toSet()
        return existing + newMeals.filter { (it.name to it.kcal to it.date) !in existingKeys }
    }

    /**
     * Persists a scan result into today's calorie log so the daily total
     * updates on the Calorie Tracker and Home screens. Returns the number
     * of meals actually added (0 when everything was already logged).
     */
    fun logScanResult(context: Context, result: FoodScanResult): Int {
        val meals = mealsFromScan(result)
        if (meals.isEmpty()) return 0
        val today = loadTodayMeals(context)
        val updated = appendLoggedFoods(today, meals)
        saveMeals(context, updated)
        return updated.size - today.size
    }
}
