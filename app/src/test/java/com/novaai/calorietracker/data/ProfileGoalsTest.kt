package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileGoalsTest {

    private val complete = UserProfile(
        name = "Alex",
        age = 34,
        sex = Sex.FEMALE,
        heightCm = 175.26f,
        currentWeightKg = 69.853f,
        goalWeightKg = 64.864f,
        mainGoal = MainGoal.LOSE_WEIGHT,
        activityLevel = ActivityLevel.MODERATELY_ACTIVE,
        dailyStepGoal = 8000,
        units = MeasurementUnits.IMPERIAL
    )

    @Test
    fun calorieTargetUsesPersonalizedPipeline() {
        // 12D device-verified value for this exact profile.
        assertEquals(1814, ProfileGoals.calorieTarget(complete))
    }

    @Test
    fun calorieTargetFallsBackForIncompleteProfile() {
        assertEquals(2000, ProfileGoals.calorieTarget(UserProfile()))
    }

    @Test
    fun stepGoalUsesSavedGoal() {
        assertEquals(8000, ProfileGoals.stepGoal(complete))
    }

    @Test
    fun stepGoalFallsBackToDefault() {
        assertEquals(10_000, ProfileGoals.stepGoal(UserProfile()))
    }

    @Test
    fun goalWeightUsesDisplayUnits() {
        // 64.864 kg == 143 lb (imperial display).
        assertEquals("143", ProfileGoals.goalWeightText(complete))
    }

    @Test
    fun goalWeightEmptyWhenUnanswered() {
        assertEquals("", ProfileGoals.goalWeightText(UserProfile()))
    }

    @Test
    fun parseWeightKeepsMetricAsKg() {
        assertEquals(70.0f, ProfileGoals.parseWeightToKg("70", false)!!, 0.001f)
    }

    @Test
    fun parseWeightConvertsImperialToKg() {
        assertEquals(64.864f, ProfileGoals.parseWeightToKg("143", true)!!, 0.01f)
    }

    @Test
    fun parseWeightRejectsGarbage() {
        assertNull(ProfileGoals.parseWeightToKg("abc", false))
        assertNull(ProfileGoals.parseWeightToKg("", true))
    }

    @Test
    fun sleepGoalAllowsZero() {
        assertTrue(ProfileGoals.validSleepGoal(0f))
        assertTrue(ProfileGoals.validSleepGoal(0.0f))
        assertTrue(ProfileGoals.validSleepGoal(8f))
        assertTrue(ProfileGoals.validSleepGoal(23.5f))
    }

    @Test
    fun sleepGoalRejectsNegativesAndUpperLimit() {
        assertFalse(ProfileGoals.validSleepGoal(-0.1f))
        assertFalse(ProfileGoals.validSleepGoal(-8f))
        assertFalse(ProfileGoals.validSleepGoal(24f))
        assertFalse(ProfileGoals.validSleepGoal(100f))
    }

    @Test
    fun sleepGoalFormatsWithoutTrailingZero() {
        assertEquals("0", ProfileGoals.formatSleepGoal(0f))
        assertEquals("8", ProfileGoals.formatSleepGoal(8f))
        assertEquals("7.5", ProfileGoals.formatSleepGoal(7.5f))
    }
}
