package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class WeightState(val kg: Float, val date: LocalDate)

/**
 * Single source of truth for the user's locally saved weight history.
 * Newest entry is the current weight. SharedPreferences JSON list, same style
 * as [CalorieStore]. The previous single current_weight_kg key is still written
 * so Home can keep reading [load], and is migrated into history when needed.
 */
object WeightStore {
    private const val PREFS_NAME = "weight_tracker"
    private const val KEY_WEIGHT_KG = "current_weight_kg"
    private const val KEY_WEIGHT_DATE = "current_weight_date"
    private const val KEY_HISTORY = "weight_history"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns the last weight the user logged, or null if none has been saved yet. */
    fun load(context: Context): WeightState? {
        val newest = WeightHistoryLogic.current(loadHistory(context)) ?: return null
        return WeightState(newest.kg, newest.localDate())
    }

    fun loadHistory(context: Context): List<WeightLog> {
        val p = prefs(context)
        val parsed = parseHistory(p.getString(KEY_HISTORY, null))
        val migrated = WeightHistoryLogic.migrateLegacyIfEmpty(
            history = parsed,
            legacyKg = p.getFloat(KEY_WEIGHT_KG, -1f).takeIf { it > 0f },
            legacyDate = p.getString(KEY_WEIGHT_DATE, null)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        )
        if (parsed.isEmpty() && migrated.isNotEmpty()) persist(p, migrated)
        return migrated
    }

    fun save(context: Context, kg: Float) {
        if (kg <= 0f) return
        val updated = WeightHistoryLogic.upsertForDay(
            logs = loadHistory(context),
            kg = kg,
            recordedAtMillis = System.currentTimeMillis()
        )
        persist(prefs(context), updated)
    }

    private fun persist(p: SharedPreferences, logs: List<WeightLog>) {
        val newest = WeightHistoryLogic.current(logs)
        val editor = p.edit().putString(KEY_HISTORY, toJson(logs))
        if (newest != null) {
            editor.putFloat(KEY_WEIGHT_KG, newest.kg)
                .putString(KEY_WEIGHT_DATE, newest.localDate().toString())
        }
        editor.apply()
    }

    private fun toJson(logs: List<WeightLog>): String {
        val array = JSONArray()
        logs.forEach { log ->
            array.put(
                JSONObject()
                    .put("kg", log.kg.toDouble())
                    .put("at", log.recordedAtMillis)
            )
        }
        return array.toString()
    }

    private fun parseHistory(raw: String?): List<WeightLog> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.optJSONObject(i) ?: return@mapNotNull null
                val kg = obj.optDouble("kg", Double.NaN).toFloat()
                if (kg <= 0f || kg.isNaN()) return@mapNotNull null
                val at = obj.optLong("at", -1L)
                if (at < 0L) return@mapNotNull null
                WeightLog(kg, at)
            }
        }.getOrDefault(emptyList())
    }
}
