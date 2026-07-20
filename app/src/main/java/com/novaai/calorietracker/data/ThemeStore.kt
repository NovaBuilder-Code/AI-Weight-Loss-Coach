package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences

/** App theme selection offered on the Settings screen. */
enum class ThemeMode { SYSTEM, DARK, LIGHT }

/**
 * Single source of truth for the locally saved theme selection.
 * Loaded at startup by MainActivity and written by the Settings screen.
 */
object ThemeStore {
    private const val PREFS_NAME = "theme"
    private const val KEY_MODE = "mode"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(context: Context): ThemeMode {
        val saved = prefs(context).getString(KEY_MODE, null)
        return ThemeMode.values().firstOrNull { it.name == saved } ?: ThemeMode.SYSTEM
    }

    fun save(context: Context, mode: ThemeMode) {
        prefs(context).edit()
            .putString(KEY_MODE, mode.name)
            .apply()
    }
}
