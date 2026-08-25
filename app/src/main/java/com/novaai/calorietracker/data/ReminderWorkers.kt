package com.novaai.calorietracker.data

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.novaai.calorietracker.R
import java.time.LocalDate
import java.time.LocalTime

/**
 * Breakfast/lunch/dinner reminders. Runs every 15 minutes, posts only inside
 * the meal windows and only once per meal per day (deduped).
 */
class MealReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val context = applicationContext
        if (!NotificationPrefsStore.load(context).meals) return Result.success()
        val meal = ReminderSchedule.mealForTime(LocalTime.now().hour) ?: return Result.success()
        val today = LocalDate.now()
        if (!ReminderSchedule.shouldSendMeal(ReminderDedupStore.lastMealDate(context, meal), today)) {
            return Result.success()
        }
        val (titleRes, bodyRes) = when (meal) {
            ReminderSchedule.Meal.BREAKFAST -> R.string.notification_meal_breakfast_title to R.string.notification_meal_breakfast_body
            ReminderSchedule.Meal.LUNCH -> R.string.notification_meal_lunch_title to R.string.notification_meal_lunch_body
            ReminderSchedule.Meal.DINNER -> R.string.notification_meal_dinner_title to R.string.notification_meal_dinner_body
        }
        NovaNotifier.show(
            context,
            NovaNotifier.notificationId(meal),
            context.getString(titleRes),
            context.getString(bodyRes)
        )
        ReminderDedupStore.markMealNotified(context, meal, today)
        return Result.success()
    }
}

/**
 * Periodic daytime hydration reminders (never at night). Runs every 15 minutes
 * but throttled by [INTERVAL_MINUTES] so it posts at most a few times per day.
 */
class HydrationReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val context = applicationContext
        if (!NotificationPrefsStore.load(context).water) return Result.success()
        if (!ReminderSchedule.isHydrationWindow(LocalTime.now().hour)) return Result.success()
        val nowMs = System.currentTimeMillis()
        if (!ReminderSchedule.shouldSendHydration(ReminderDedupStore.lastHydrationMs(context), nowMs, INTERVAL_MINUTES)) {
            return Result.success()
        }
        NovaNotifier.show(
            context,
            NovaNotifier.ID_HYDRATION,
            context.getString(R.string.notification_hydration_title),
            context.getString(R.string.notification_hydration_body)
        )
        ReminderDedupStore.markHydrationNotified(context, nowMs)
        return Result.success()
    }

    companion object {
        /** At most one hydration reminder every 3 hours of daytime. */
        const val INTERVAL_MINUTES = 180L
    }
}

/** Weekly weigh-in reminder. Runs every 24h, deduped to once every 7 days. */
class WeighInReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val context = applicationContext
        if (!NotificationPrefsStore.load(context).weighIn) return Result.success()
        val today = LocalDate.now()
        if (!ReminderSchedule.isWeighInDue(ReminderDedupStore.lastWeighInDate(context), today)) {
            return Result.success()
        }
        NovaNotifier.show(
            context,
            NovaNotifier.ID_WEIGH_IN,
            context.getString(R.string.notification_weighin_title),
            context.getString(R.string.notification_weighin_body)
        )
        ReminderDedupStore.markWeighInNotified(context, today)
        return Result.success()
    }
}

/** One morning motivation notification per day. Runs every 15 minutes. */
class MotivationReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val context = applicationContext
        if (!NotificationPrefsStore.load(context).motivation) return Result.success()
        if (!ReminderSchedule.isMotivationHour(LocalTime.now().hour)) return Result.success()
        val today = LocalDate.now()
        if (!ReminderSchedule.shouldSendMotivation(ReminderDedupStore.lastMotivationDate(context), today)) {
            return Result.success()
        }
        NovaNotifier.show(
            context,
            NovaNotifier.ID_MOTIVATION,
            context.getString(R.string.notification_motivation_title),
            context.getString(R.string.notification_motivation_body)
        )
        ReminderDedupStore.markMotivationNotified(context, today)
        return Result.success()
    }
}

/**
 * Step Goal Alerts are scheduled/cancelled like the other reminders, but they
 * are intentionally a NO-OP for now: the app has no reliable live/background
 * step source yet, so we must not fake step progress. This worker keeps the
 * scheduling plumbing in place and will be filled in once a real step source
 * (e.g. a foreground SensorListener / Health platform) lands.
 */
class StepGoalReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result = Result.success()
}
