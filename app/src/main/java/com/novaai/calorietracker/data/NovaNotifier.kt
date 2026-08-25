package com.novaai.calorietracker.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.novaai.calorietracker.R

/**
 * Creates the reminder notification channel and posts the actual Nova
 * notifications. Tapping a notification opens the app (launcher intent).
 */
object NovaNotifier {
    const val CHANNEL_ID = "nova_reminders"

    // Stable, unique per-type notification ids.
    const val ID_MEAL_BREAKFAST = 1
    const val ID_MEAL_LUNCH = 2
    const val ID_MEAL_DINNER = 3
    const val ID_HYDRATION = 4
    const val ID_WEIGH_IN = 5
    const val ID_MOTIVATION = 6

    fun notificationId(meal: ReminderSchedule.Meal): Int = when (meal) {
        ReminderSchedule.Meal.BREAKFAST -> ID_MEAL_BREAKFAST
        ReminderSchedule.Meal.LUNCH -> ID_MEAL_LUNCH
        ReminderSchedule.Meal.DINNER -> ID_MEAL_DINNER
    }

    /** Idempotent — safe to call on every app start. */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /** Posts a reminder; silently no-ops on Android 13+ when permission is denied. */
    fun show(context: Context, id: Int, title: String, body: String) {
        val contentIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.let {
                PendingIntent.getActivity(
                    context,
                    id,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
