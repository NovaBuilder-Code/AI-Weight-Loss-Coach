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
    fun rebootDoesNotAddBootCountToToday() {
        val tick = StepCounterMath.onTick(todaySteps = 200, lastCounter = 9_000L, counter = 15L)
        assertEquals(200, tick.todaySteps)
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

    @Test
    fun firstCounterIsBaselineZeroToday() {
        val s = StepCounterMath.onCounter(NativeStepState(), 66_836L)
        assertEquals(0, s.todaySteps)
        assertEquals(66_836L, s.baselineCounter)
        assertEquals(66_836L, s.lastCounter)
    }

    @Test
    fun fourDetectorEventsCountFour() {
        var s = StepCounterMath.onCounter(NativeStepState(), 66_836L)
        repeat(4) { s = StepCounterMath.onDetector(s) }
        assertEquals(4, s.todaySteps)
        assertEquals(4, s.detectorSince)
    }

    @Test
    fun detectorThenCounterDoesNotDoubleCount() {
        var s = StepCounterMath.onCounter(NativeStepState(), 66_836L)
        repeat(4) { s = StepCounterMath.onDetector(s) }
        s = StepCounterMath.onCounter(s, 66_840L)
        assertEquals(4, s.todaySteps)
    }

    @Test
    fun counterCatchupBeatsConservativeDetector() {
        var s = StepCounterMath.onCounter(NativeStepState(), 66_836L)
        repeat(4) { s = StepCounterMath.onDetector(s) }
        s = StepCounterMath.onCounter(s, 66_860L)
        assertEquals(24, s.todaySteps)
    }

    @Test
    fun rebootDoesNotCopyHardwareCountIntoToday() {
        val prior = NativeStepState(todaySteps = 0, lastCounter = 66_836L)
        val s = StepCounterMath.onCounter(prior, 79L)
        assertEquals(0, s.todaySteps)
        assertEquals(79L, s.baselineCounter)
        assertEquals(79L, s.lastCounter)
    }

    @Test
    fun startSessionClearsBootCountCopiedAsToday() {
        val corrupt = NativeStepState(
            todaySteps = 79,
            lastCounter = 79L,
            baselineCounter = 79L,
            detectorSince = 0,
            baselineToday = 79
        )
        val s = StepCounterMath.startSession(corrupt, 79L)
        assertEquals(0, s.todaySteps)
        assertEquals(79L, s.baselineCounter)
        assertEquals(0, s.detectorSince)
    }
}
