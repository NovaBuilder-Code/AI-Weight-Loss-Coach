package com.novaai.calorietracker.data

import kotlin.math.roundToInt

/**
 * Pure, testable calorie math for Task 12C.
 *
 * BMR via the Mifflin–St Jeor equation, TDEE = BMR × activity multiplier,
 * and the daily target = TDEE adjusted by the onboarding main goal, then
 * clamped to safe limits so it can never become unrealistically low or high.
 */
object CalorieCalculator {

    /** Fallback target when the onboarding profile is incomplete. */
    const val DEFAULT_DAILY_TARGET = 2000

    /** Floors recommended for safe weight management (kcal/day). */
    const val MIN_DAILY_TARGET_MALE = 1500
    const val MIN_DAILY_TARGET_FEMALE = 1200

    /** Ceiling applied to any calculated target (kcal/day). */
    const val MAX_DAILY_TARGET = 5000

    /** 20% deficit ≈ a safe ~0.5 kg/week weight loss. */
    const val LOSE_WEIGHT_FACTOR = 0.80f
    const val MAINTAIN_WEIGHT_FACTOR = 1.00f

    /** 10% surplus keeps weight gain controlled (~0.25 kg/week). */
    const val GAIN_WEIGHT_FACTOR = 1.10f

    /**
     * Mifflin–St Jeor: 10 × weight(kg) + 6.25 × height(cm) − 5 × age + offset,
     * where offset is +5 for men and −161 for women.
     */
    fun calculateBmr(sex: Sex, age: Int, heightCm: Float, weightKg: Float): Float {
        val base = 10f * weightKg + 6.25f * heightCm - 5f * age
        return when (sex) {
            Sex.MALE -> base + 5f
            Sex.FEMALE -> base - 161f
        }
    }

    /** Standard activity multipliers used for TDEE estimation. */
    fun activityMultiplier(level: ActivityLevel): Float = when (level) {
        ActivityLevel.SEDENTARY -> 1.2f
        ActivityLevel.LIGHTLY_ACTIVE -> 1.375f
        ActivityLevel.MODERATELY_ACTIVE -> 1.55f
        ActivityLevel.VERY_ACTIVE -> 1.725f
    }

    /** TDEE = BMR × activity multiplier. */
    fun calculateTdee(bmr: Float, level: ActivityLevel): Float =
        bmr * activityMultiplier(level)

    /** Goal adjustment applied to TDEE: deficit / maintenance / surplus. */
    fun goalFactor(goal: MainGoal): Float = when (goal) {
        MainGoal.LOSE_WEIGHT -> LOSE_WEIGHT_FACTOR
        MainGoal.MAINTAIN_WEIGHT -> MAINTAIN_WEIGHT_FACTOR
        MainGoal.GAIN_WEIGHT -> GAIN_WEIGHT_FACTOR
    }

    /** The safe floor depends on sex (men need more than women). */
    fun minimumSafeTarget(sex: Sex): Int =
        if (sex == Sex.MALE) MIN_DAILY_TARGET_MALE else MIN_DAILY_TARGET_FEMALE

    /**
     * Full pipeline from the saved onboarding profile. Returns null when any
     * required field is missing (incomplete profile) so callers can fall back.
     */
    fun calculateDailyTarget(profile: UserProfile): Int? {
        val sex = profile.sex ?: return null
        val age = profile.age ?: return null
        val heightCm = profile.heightCm ?: return null
        val weightKg = profile.currentWeightKg ?: return null
        val activity = profile.activityLevel ?: return null
        val goal = profile.mainGoal ?: return null
        return calculateDailyTarget(sex, age, heightCm, weightKg, activity, goal)
    }

    /**
     * BMR → TDEE → goal-adjusted target, rounded and clamped to
     * [minimumSafeTarget]..[MAX_DAILY_TARGET].
     */
    fun calculateDailyTarget(
        sex: Sex,
        age: Int,
        heightCm: Float,
        weightKg: Float,
        activity: ActivityLevel,
        goal: MainGoal
    ): Int {
        val tdee = calculateTdee(calculateBmr(sex, age, heightCm, weightKg), activity)
        val adjusted = tdee * goalFactor(goal)
        return adjusted.roundToInt().coerceIn(minimumSafeTarget(sex), MAX_DAILY_TARGET)
    }
}