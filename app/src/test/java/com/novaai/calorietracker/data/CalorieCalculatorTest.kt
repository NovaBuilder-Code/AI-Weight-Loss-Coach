package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalorieCalculatorTest {

    // --- BMR (Mifflin–St Jeor) ---

    @Test
    fun bmrMaleMatchesMifflinStJeor() {
        // 10×70 + 6.25×175 − 5×30 + 5 = 1648.75
        assertEquals(1648.75f, CalorieCalculator.calculateBmr(Sex.MALE, 30, 175f, 70f), 0.001f)
    }

    @Test
    fun bmrFemaleMatchesMifflinStJeor() {
        // 10×60 + 6.25×165 − 5×30 − 161 = 1320.25
        assertEquals(1320.25f, CalorieCalculator.calculateBmr(Sex.FEMALE, 30, 165f, 60f), 0.001f)
    }

    // --- Activity multipliers ---

    @Test
    fun activityMultipliersAreStandard() {
        assertEquals(1.2f, CalorieCalculator.activityMultiplier(ActivityLevel.SEDENTARY), 0f)
        assertEquals(1.375f, CalorieCalculator.activityMultiplier(ActivityLevel.LIGHTLY_ACTIVE), 0f)
        assertEquals(1.55f, CalorieCalculator.activityMultiplier(ActivityLevel.MODERATELY_ACTIVE), 0f)
        assertEquals(1.725f, CalorieCalculator.activityMultiplier(ActivityLevel.VERY_ACTIVE), 0f)
    }

    @Test
    fun tdeeIsBmrTimesMultiplier() {
        val bmr = CalorieCalculator.calculateBmr(Sex.MALE, 30, 175f, 70f)
        assertEquals(1648.75f * 1.55f, CalorieCalculator.calculateTdee(bmr, ActivityLevel.MODERATELY_ACTIVE), 0.001f)
        assertEquals(1648.75f * 1.2f, CalorieCalculator.calculateTdee(bmr, ActivityLevel.SEDENTARY), 0.001f)
    }

    // --- Goal adjustments ---

    @Test
    fun goalFactorsApplyDeficitMaintenanceSurplus() {
        assertEquals(0.80f, CalorieCalculator.goalFactor(MainGoal.LOSE_WEIGHT), 0f)
        assertEquals(1.00f, CalorieCalculator.goalFactor(MainGoal.MAINTAIN_WEIGHT), 0f)
        assertEquals(1.10f, CalorieCalculator.goalFactor(MainGoal.GAIN_WEIGHT), 0f)
    }

    // --- Full daily-target pipeline (device-style profile) ---

    private val deviceProfile = UserProfile(
        name = "Alex",
        age = 34,
        sex = Sex.FEMALE,
        heightCm = 175.26f,
        currentWeightKg = 69.853f,
        goalWeightKg = 64.864f,
        mainGoal = MainGoal.LOSE_WEIGHT,
        activityLevel = ActivityLevel.MODERATELY_ACTIVE,
        dailyStepGoal = 10_000,
        units = MeasurementUnits.IMPERIAL
    )

    @Test
    fun loseWeightTargetIsTwentyPercentDeficit() {
        // BMR 1462.905 → TDEE ×1.55 = 2267.50 → ×0.80 = 1814.00 → 1814 kcal
        assertEquals(1814, CalorieCalculator.calculateDailyTarget(deviceProfile))
    }

    @Test
    fun maintainWeightTargetIsTdee() {
        val maintain = deviceProfile.copy(mainGoal = MainGoal.MAINTAIN_WEIGHT)
        assertEquals(2268, CalorieCalculator.calculateDailyTarget(maintain))
    }

    @Test
    fun gainWeightTargetIsTenPercentSurplus() {
        val gain = deviceProfile.copy(mainGoal = MainGoal.GAIN_WEIGHT)
        assertEquals(2494, CalorieCalculator.calculateDailyTarget(gain))
    }

    // --- Safety limits ---

    @Test
    fun femaleTargetClampedToSafeFloor() {
        // Tiny/frail profile would produce ~611 kcal; floor for women is 1200.
        val tiny = UserProfile(
            age = 13, sex = Sex.FEMALE, heightCm = 90f, currentWeightKg = 30f,
            mainGoal = MainGoal.LOSE_WEIGHT, activityLevel = ActivityLevel.SEDENTARY
        )
        assertEquals(CalorieCalculator.MIN_DAILY_TARGET_FEMALE, CalorieCalculator.calculateDailyTarget(tiny))
    }

    @Test
    fun maleTargetClampedToHigherFloor() {
        // Same tiny inputs as a man would clamp to the male floor of 1500.
        val tiny = UserProfile(
            age = 13, sex = Sex.MALE, heightCm = 90f, currentWeightKg = 30f,
            mainGoal = MainGoal.LOSE_WEIGHT, activityLevel = ActivityLevel.SEDENTARY
        )
        assertEquals(CalorieCalculator.MIN_DAILY_TARGET_MALE, CalorieCalculator.calculateDailyTarget(tiny))
    }

    @Test
    fun targetClampedToCeiling() {
        // Extreme profile would produce ~7528 kcal; ceiling is 5000.
        val huge = UserProfile(
            age = 120, sex = Sex.MALE, heightCm = 250f, currentWeightKg = 300f,
            mainGoal = MainGoal.GAIN_WEIGHT, activityLevel = ActivityLevel.VERY_ACTIVE
        )
        assertEquals(CalorieCalculator.MAX_DAILY_TARGET, CalorieCalculator.calculateDailyTarget(huge))
    }

    @Test
    fun targetNeverBelowFloorOrAboveCeilingAcrossInputGrid() {
        for (sex in Sex.entries) {
            for (level in ActivityLevel.entries) {
                for (goal in MainGoal.entries) {
                    val target = CalorieCalculator.calculateDailyTarget(
                        sex, 13, 90f, 30f, level, goal
                    )
                    assert(target >= CalorieCalculator.minimumSafeTarget(sex))
                    assert(target <= CalorieCalculator.MAX_DAILY_TARGET)
                }
            }
        }
    }

    // --- Incomplete profile ---

    @Test
    fun incompleteProfileReturnsNull() {
        assertNull(CalorieCalculator.calculateDailyTarget(UserProfile(name = "Alex")))
        assertNull(CalorieCalculator.calculateDailyTarget(deviceProfile.copy(activityLevel = null)))
        assertNull(CalorieCalculator.calculateDailyTarget(deviceProfile.copy(mainGoal = null)))
        assertNull(CalorieCalculator.calculateDailyTarget(deviceProfile.copy(sex = null)))
    }
}