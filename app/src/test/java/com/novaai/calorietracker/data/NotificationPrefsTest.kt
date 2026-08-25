package com.novaai.calorietracker.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPrefsTest {

    @Test
    fun defaultsAreExplicitForNewUser() {
        val d = NotificationPrefs()
        // Core habit reminders on by default; weekly weigh-in prompt off.
        assertTrue(d.meals)
        assertTrue(d.water)
        assertTrue(d.steps)
        assertTrue(d.motivation)
        assertFalse(d.weighIn)
    }

    @Test
    fun togglingOneFieldKeepsTheOthers() {
        val d = NotificationPrefs().copy(meals = false)
        assertFalse(d.meals)
        assertTrue(d.water)
        assertTrue(d.steps)
        assertTrue(d.motivation)
        assertFalse(d.weighIn)

        val e = d.copy(weighIn = true)
        assertFalse(e.meals)
        assertTrue(e.weighIn)
        assertTrue(e.water)
        assertTrue(e.steps)
        assertTrue(e.motivation)
    }
}
