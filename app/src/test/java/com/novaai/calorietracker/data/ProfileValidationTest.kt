package com.novaai.calorietracker.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileValidationTest {

    @Test
    fun nameMustNotBeBlank() {
        assertTrue(ProfileValidation.validName("Alex"))
        assertTrue(ProfileValidation.validName("  Alex  "))
        assertFalse(ProfileValidation.validName(""))
        assertFalse(ProfileValidation.validName("   "))
    }

    @Test
    fun ageUsesOnboardingRange() {
        assertTrue(ProfileValidation.validAge(13))
        assertTrue(ProfileValidation.validAge(34))
        assertTrue(ProfileValidation.validAge(120))
        assertFalse(ProfileValidation.validAge(12))
        assertFalse(ProfileValidation.validAge(121))
        assertFalse(ProfileValidation.validAge(0))
    }

    @Test
    fun heightUsesOnboardingRange() {
        assertTrue(ProfileValidation.validHeightCm(90f))
        assertTrue(ProfileValidation.validHeightCm(175.26f))
        assertTrue(ProfileValidation.validHeightCm(250f))
        assertFalse(ProfileValidation.validHeightCm(89.9f))
        assertFalse(ProfileValidation.validHeightCm(250.1f))
    }

    @Test
    fun weightUsesOnboardingRange() {
        assertTrue(ProfileValidation.validWeightKg(30f))
        assertTrue(ProfileValidation.validWeightKg(69.853f))
        assertTrue(ProfileValidation.validWeightKg(300f))
        assertFalse(ProfileValidation.validWeightKg(29.9f))
        assertFalse(ProfileValidation.validWeightKg(300.1f))
    }

    @Test
    fun stepGoalUsesOnboardingRange() {
        assertTrue(ProfileValidation.validStepGoal(1000))
        assertTrue(ProfileValidation.validStepGoal(10_000))
        assertTrue(ProfileValidation.validStepGoal(100_000))
        assertFalse(ProfileValidation.validStepGoal(999))
        assertFalse(ProfileValidation.validStepGoal(100_001))
        assertFalse(ProfileValidation.validStepGoal(0))
    }
}