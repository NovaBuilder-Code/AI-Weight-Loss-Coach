package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences

/**
 * The user's reminder preferences (Notifications screen). This is only the
 * saved settings layer — no scheduling/alarms/notifications are scheduled
 * from here.
 *
 * Defaults for a brand-new user: the core habit reminders are on, the weekly
 * weigh-in prompt is off.
 */
data class NotificationPrefs(
    val meals: Boolean = true,
    val water: Boolean = true,
    val steps: Boolean = true,
    val weighIn: Boolean = false,
    val motivation: Boolean = true
)

/**
 * Single source of truth for the locally saved notification preferences.
 * Written immediately whenever a toggle changes so the value survives
 * leaving the screen and force-closing the app.
 */
object NotificationPrefsStore {
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_MEALS = "meals"
    private const val KEY_WATER = "water"
    private const val KEY_STEPS = "steps"
    private const val KEY_WEIGH_IN = "weigh_in"
    private const val KEY_MOTIVATION = "motivation"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(context: Context): NotificationPrefs {
        val defaults = NotificationPrefs()
        val p = prefs(context)
        return NotificationPrefs(
            meals = p.getBoolean(KEY_MEALS, defaults.meals),
            water = p.getBoolean(KEY_WATER, defaults.water),
            steps = p.getBoolean(KEY_STEPS, defaults.steps),
            weighIn = p.getBoolean(KEY_WEIGH_IN, defaults.weighIn),
            motivation = p.getBoolean(KEY_MOTIVATION, defaults.motivation)
        )
    }

    fun save(context: Context, prefs: NotificationPrefs) {
        prefs(context).edit()
            .putBoolean(KEY_MEALS, prefs.meals)
            .putBoolean(KEY_WATER, prefs.water)
            .putBoolean(KEY_STEPS, prefs.steps)
            .putBoolean(KEY_WEIGH_IN, prefs.weighIn)
            .putBoolean(KEY_MOTIVATION, prefs.motivation)
            .apply()
    }
}
