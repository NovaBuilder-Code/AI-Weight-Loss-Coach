package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences

/** Sex used for calorie calculations during onboarding. */
enum class Sex { MALE, FEMALE }

/** The user's main goal chosen during onboarding. */
enum class MainGoal { LOSE_WEIGHT, MAINTAIN_WEIGHT, GAIN_WEIGHT }

/** Self-reported activity level used for calorie calculations. */
enum class ActivityLevel { SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE }

/** Preferred measurement system for displaying values. */
enum class MeasurementUnits { METRIC, IMPERIAL }

/**
 * The user's onboarding answers / personalized profile.
 * A null field means the user has not answered that question yet.
 * Height and weights are always stored in metric (cm / kg); [units] only
 * controls how values are displayed and entered in the UI.
 */
data class UserProfile(
    val name: String? = null,
    val age: Int? = null,
    val sex: Sex? = null,
    val heightCm: Float? = null,
    val currentWeightKg: Float? = null,
    val goalWeightKg: Float? = null,
    val mainGoal: MainGoal? = null,
    val activityLevel: ActivityLevel? = null,
    val dailyStepGoal: Int? = null,
    val units: MeasurementUnits? = null
)

/**
 * Single source of truth for the locally saved onboarding profile.
 * Follows the same SharedPreferences store pattern as the other data stores.
 */
object UserProfileStore {
    private const val PREFS_NAME = "user_profile"
    private const val KEY_NAME = "name"
    private const val KEY_AGE = "age"
    private const val KEY_SEX = "sex"
    private const val KEY_HEIGHT_CM = "height_cm"
    private const val KEY_CURRENT_WEIGHT_KG = "current_weight_kg"
    private const val KEY_GOAL_WEIGHT_KG = "goal_weight_kg"
    private const val KEY_MAIN_GOAL = "main_goal"
    private const val KEY_ACTIVITY_LEVEL = "activity_level"
    private const val KEY_DAILY_STEP_GOAL = "daily_step_goal"
    private const val KEY_UNITS = "units"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Persists the whole profile; null fields are removed (treated as unanswered). */
    fun save(context: Context, profile: UserProfile) {
        prefs(context).edit().apply {
            putOrRemoveString(KEY_NAME, profile.name?.trim()?.takeIf { it.isNotEmpty() })
            putOrRemoveInt(KEY_AGE, profile.age?.takeIf { it > 0 })
            putOrRemoveString(KEY_SEX, profile.sex?.name)
            putOrRemoveFloat(KEY_HEIGHT_CM, profile.heightCm?.takeIf { it > 0f })
            putOrRemoveFloat(KEY_CURRENT_WEIGHT_KG, profile.currentWeightKg?.takeIf { it > 0f })
            putOrRemoveFloat(KEY_GOAL_WEIGHT_KG, profile.goalWeightKg?.takeIf { it > 0f })
            putOrRemoveString(KEY_MAIN_GOAL, profile.mainGoal?.name)
            putOrRemoveString(KEY_ACTIVITY_LEVEL, profile.activityLevel?.name)
            putOrRemoveInt(KEY_DAILY_STEP_GOAL, profile.dailyStepGoal?.takeIf { it > 0 })
            putOrRemoveString(KEY_UNITS, profile.units?.name)
        }.apply()
    }

    /** Returns the saved profile; fields the user never answered are null. */
    fun load(context: Context): UserProfile {
        val p = prefs(context)
        return UserProfile(
            name = p.getString(KEY_NAME, null),
            age = p.getInt(KEY_AGE, -1).takeIf { it > 0 },
            sex = enumOrNull<Sex>(p.getString(KEY_SEX, null)),
            heightCm = p.getFloat(KEY_HEIGHT_CM, -1f).takeIf { it > 0f },
            currentWeightKg = p.getFloat(KEY_CURRENT_WEIGHT_KG, -1f).takeIf { it > 0f },
            goalWeightKg = p.getFloat(KEY_GOAL_WEIGHT_KG, -1f).takeIf { it > 0f },
            mainGoal = enumOrNull<MainGoal>(p.getString(KEY_MAIN_GOAL, null)),
            activityLevel = enumOrNull<ActivityLevel>(p.getString(KEY_ACTIVITY_LEVEL, null)),
            dailyStepGoal = p.getInt(KEY_DAILY_STEP_GOAL, -1).takeIf { it > 0 },
            units = enumOrNull<MeasurementUnits>(p.getString(KEY_UNITS, null))
        )
    }

    /** Removes every saved profile field. */
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
        enumValues<T>().firstOrNull { it.name == name }

    private fun SharedPreferences.Editor.putOrRemoveString(key: String, value: String?) {
        if (value != null) putString(key, value) else remove(key)
    }

    private fun SharedPreferences.Editor.putOrRemoveInt(key: String, value: Int?) {
        if (value != null) putInt(key, value) else remove(key)
    }

    private fun SharedPreferences.Editor.putOrRemoveFloat(key: String, value: Float?) {
        if (value != null) putFloat(key, value) else remove(key)
    }
}
