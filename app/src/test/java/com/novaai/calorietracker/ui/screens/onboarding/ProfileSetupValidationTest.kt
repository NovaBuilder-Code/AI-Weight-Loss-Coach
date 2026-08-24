package com.novaai.calorietracker.ui.screens.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileSetupValidationTest {

    @Test
    fun `step goal constants define a sensible range`() {
        assertTrue(MIN_STEP_GOAL > 0)
        assertTrue(MAX_STEP_GOAL > MIN_STEP_GOAL)
    }

    @Test
    fun `default step goal is ten thousand and inside the valid range`() {
        assertEquals(10000, DEFAULT_STEP_GOAL)
        assertTrue(DEFAULT_STEP_GOAL in MIN_STEP_GOAL..MAX_STEP_GOAL)
    }

    @Test
    fun `range boundaries are acceptable step goals`() {
        assertTrue(MIN_STEP_GOAL in MIN_STEP_GOAL..MAX_STEP_GOAL)
        assertTrue(MAX_STEP_GOAL in MIN_STEP_GOAL..MAX_STEP_GOAL)
    }
}