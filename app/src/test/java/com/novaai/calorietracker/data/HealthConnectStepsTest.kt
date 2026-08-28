package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class HealthConnectStepsTest {

    private val stockholm = ZoneId.of("Europe/Stockholm")

    @Test
    fun todayRangeStartsAtLocalMidnightInDeviceZone() {
        val now = ZonedDateTime.of(2026, 8, 29, 12, 18, 0, 0, stockholm)
        val (start, end) = HealthConnectSteps.todayRange(now)
        val startLocal = start.atZone(stockholm)
        assertEquals(2026, startLocal.year)
        assertEquals(8, startLocal.monthValue)
        assertEquals(29, startLocal.dayOfMonth)
        assertEquals(0, startLocal.hour)
        assertEquals(0, startLocal.minute)
        assertEquals(now.toInstant(), end)
    }

    @Test
    fun todayRangeUsesCallerZoneNotUtc() {
        val zone = ZoneId.of("America/Los_Angeles")
        val now = ZonedDateTime.of(2026, 8, 28, 23, 30, 0, 0, zone)
        val (start, _) = HealthConnectSteps.todayRange(now)
        val startLocal = start.atZone(zone)
        assertEquals(28, startLocal.dayOfMonth)
        assertEquals(0, startLocal.hour)
    }

    @Test
    fun aggregateNullOrNegativeIsZero() {
        assertEquals(0, HealthConnectSteps.stepsFromAggregate(null))
        assertEquals(0, HealthConnectSteps.stepsFromAggregate(0L))
        assertEquals(0, HealthConnectSteps.stepsFromAggregate(-5L))
    }

    @Test
    fun aggregatePositiveCountIsUsed() {
        assertEquals(4321, HealthConnectSteps.stepsFromAggregate(4321L))
        assertEquals(Int.MAX_VALUE, HealthConnectSteps.stepsFromAggregate(Int.MAX_VALUE.toLong() + 10L))
    }

    @Test
    fun unavailableFailsClosedAtZero() {
        val read = HealthConnectSteps.result(
            HealthConnectAvailability.UNAVAILABLE,
            permissionGranted = true,
            aggregateCount = 9999L
        )
        assertEquals(0, read.steps)
        assertEquals(TodayStepsStatus.UNAVAILABLE, read.status)
    }

    @Test
    fun updateRequiredFailsClosedAtZero() {
        val read = HealthConnectSteps.result(
            HealthConnectAvailability.UPDATE_REQUIRED,
            permissionGranted = true,
            aggregateCount = 9999L
        )
        assertEquals(0, read.steps)
        assertEquals(TodayStepsStatus.UPDATE_REQUIRED, read.status)
    }

    @Test
    fun permissionDeniedFailsClosedAtZero() {
        val read = HealthConnectSteps.result(
            HealthConnectAvailability.AVAILABLE,
            permissionGranted = false,
            aggregateCount = 8000L
        )
        assertEquals(0, read.steps)
        assertEquals(TodayStepsStatus.PERMISSION_REQUIRED, read.status)
    }

    @Test
    fun readErrorFailsClosedAtZero() {
        val read = HealthConnectSteps.result(
            HealthConnectAvailability.AVAILABLE,
            permissionGranted = true,
            aggregateCount = 8000L,
            readError = true
        )
        assertEquals(0, read.steps)
        assertEquals(TodayStepsStatus.ERROR, read.status)
    }

    @Test
    fun grantedWithNoRecordsTodayIsZeroOk() {
        val read = HealthConnectSteps.result(
            HealthConnectAvailability.AVAILABLE,
            permissionGranted = true,
            aggregateCount = null
        )
        assertEquals(0, read.steps)
        assertEquals(TodayStepsStatus.OK, read.status)
        assertEquals(false, read.hasRecords)
    }

    @Test
    fun grantedWithRecordsShowsRealTotal() {
        val read = HealthConnectSteps.result(
            HealthConnectAvailability.AVAILABLE,
            permissionGranted = true,
            aggregateCount = 6543L
        )
        assertEquals(6543, read.steps)
        assertEquals(TodayStepsStatus.OK, read.status)
    }
}