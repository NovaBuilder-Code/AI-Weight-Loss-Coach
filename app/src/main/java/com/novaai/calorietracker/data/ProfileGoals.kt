package com.novaai.calorietracker.data

import com.novaai.calorietracker.ui.screens.onboarding.DEFAULT_STEP_GOAL

/**
 * Pure helpers for the Profile → Goals screen (Task 13C): the real saved goal
 * values that screen shows and updates. The calorie goal comes from the
 * personalized pipeline (Task 12C); the step goal and goal weight come from
 * the saved onboarding profile.
 */
object ProfileGoals {

    /** Personalized daily calorie target, or the 2000 fallback when the profile is incomplete. */
    fun calorieTarget(profile: UserProfile): Int =
        CalorieCalculator.calculateDailyTarget(profile) ?: CalorieCalculator.DEFAULT_DAILY_TARGET

    /** Saved daily step goal, or the onboarding default (10,000). */
    fun stepGoal(profile: UserProfile): Int =
        profile.dailyStepGoal ?: DEFAULT_STEP_GOAL

    /** Goal weight text in the profile's display units ("" when unanswered). */
    fun goalWeightText(profile: UserProfile): String {
        val kg = profile.goalWeightKg ?: return ""
        return ProfileDisplay.weightValue(kg, profile.units == MeasurementUnits.IMPERIAL)
    }

    /** Parses a weight the user typed (in the profile's display units) back to canonical kg. */
    fun parseWeightToKg(text: String, imperial: Boolean): Float? {
        val value = text.trim().replace(',', '.').toFloatOrNull() ?: return null
        return if (imperial) UnitConversion.lbToKg(value) else value
    }
}
