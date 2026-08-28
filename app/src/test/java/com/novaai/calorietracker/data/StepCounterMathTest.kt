package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Test

class StepCounterMathTest {

    @Test
    fun firstSampleBaselinesWithoutAddingLifetimeCount() {
        val tick = StepCounterMath.onTick(todaySteps = 0, lastCounter = -1L, counter = 50_000L)
        assertEquals(0, tick.todaySteps)
        assertEquals(50_000L, tick.lastCounter)
    }

    @Test
    fun laterSampleAddsOnlyTheDelta() {
        val tick = StepCounterMath.onTick(todaySteps = 10, lastCounter = 100L, counter = 130L)
        assertEquals(40, tick.todaySteps)
        assertEquals(130L, tick.lastCounter)
    }

    @Test
    fun rebootAddsCounterSinceBootNotANegativeJump() {
        val tick = StepCounterMath.onTick(todaySteps = 200, lastCounter = 9_000L, counter = 15L)
        assertEquals(215, tick.todaySteps)
        assertEquals(15L, tick.lastCounter)
    }

    @Test
    fun preferHealthConnectWhenItHasRecords() {
        val hc = TodayStepsRead(80, TodayStepsStatus.OK, hasRecords = true)
        assertEquals(80, HealthConnectSteps.displayedSteps(hc, sensorToday = 25))
    }

    @Test
    fun useSensorWhenHealthConnectHasNoRecords() {
        val hc = TodayStepsRead(0, TodayStepsStatus.OK, hasRecords = false)
        assertEquals(25, HealthConnectSteps.displayedSteps(hc, sensorToday = 25))
    }

    @Test
    fun neverSumBothSources() {
        val hc = TodayStepsRead(80, TodayStepsStatus.OK, hasRecords = true)
        assertEquals(80, HealthConnectSteps.displayedSteps(hc, sensorToday = 80))
    }
}
