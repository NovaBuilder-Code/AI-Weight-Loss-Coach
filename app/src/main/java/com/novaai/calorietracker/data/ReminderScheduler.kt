package com.novaai.calorietracker.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Isolates all WorkManager scheduling from the UI. Enable/disable is
 * idempotent:
 *  - unique, stable work names per reminder type,
 *  - REPLACE policy when (re)enabling, so repeated toggles never duplicate,
 *  - cancelUniqueWork when disabling,
 *  - a full syncFromPrefs on every app start (MainActivity) reconciles
 *    anything left behind after a restart.
 */
object ReminderScheduler {
    const val WORK_MEALS = "nova_reminder_meals"
    const val WORK_HYDRATION = "nova_reminder_hydration"
    const val WORK_WEIGH_IN = "nova_reminder_weigh_in"
    const val WORK_MOTIVATION = "nova_reminder_motivation"
    const val WORK_STEP_GOAL = "nova_reminder_step_goal"

    private const val WINDOW_INTERVAL_MINUTES = 15L
    private const val DAY_INTERVAL_MINUTES = 24L * 60

    /** Reconciles scheduled work with the saved preferences. */
    fun syncFromPrefs(context: Context, prefs: NotificationPrefs) {
        scheduleOrCancel(context, WORK_MEALS, prefs.meals) {
            PeriodicWorkRequestBuilder<MealReminderWorker>(WINDOW_INTERVAL_MINUTES, TimeUnit.MINUTES).build()
        }
        scheduleOrCancel(context, WORK_HYDRATION, prefs.water) {
            PeriodicWorkRequestBuilder<HydrationReminderWorker>(WINDOW_INTERVAL_MINUTES, TimeUnit.MINUTES).build()
        }
        scheduleOrCancel(context, WORK_WEIGH_IN, prefs.weighIn) {
            PeriodicWorkRequestBuilder<WeighInReminderWorker>(DAY_INTERVAL_MINUTES, TimeUnit.MINUTES).build()
        }
        scheduleOrCancel(context, WORK_MOTIVATION, prefs.motivation) {
            PeriodicWorkRequestBuilder<MotivationReminderWorker>(WINDOW_INTERVAL_MINUTES, TimeUnit.MINUTES).build()
        }
        scheduleOrCancel(context, WORK_STEP_GOAL, prefs.steps) {
            PeriodicWorkRequestBuilder<StepGoalReminderWorker>(DAY_INTERVAL_MINUTES, TimeUnit.MINUTES).build()
        }
    }

    private inline fun scheduleOrCancel(
        context: Context,
        name: String,
        enabled: Boolean,
        request: () -> PeriodicWorkRequest
    ) {
        val wm = WorkManager.getInstance(context)
        if (enabled) {
            wm.enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.REPLACE, request())
        } else {
            wm.cancelUniqueWork(name)
        }
    }
}
