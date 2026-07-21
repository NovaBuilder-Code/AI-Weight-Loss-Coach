package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences

/** App language selection offered on the Settings screen (beta languages). */
enum class AppLanguage { ENGLISH, NORSK, SVENSKA }

/**
 * Single source of truth for the locally saved language selection.
 * Only stores the choice for now — the app locale is not changed yet.
 */
object LanguageStore {
    private const val PREFS_NAME = "language"
    private const val KEY_LANGUAGE = "language"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(context: Context): AppLanguage {
        val saved = prefs(context).getString(KEY_LANGUAGE, null)
        return AppLanguage.values().firstOrNull { it.name == saved } ?: AppLanguage.ENGLISH
    }

    fun save(context: Context, language: AppLanguage) {
        prefs(context).edit()
            .putString(KEY_LANGUAGE, language.name)
            .apply()
    }
}
