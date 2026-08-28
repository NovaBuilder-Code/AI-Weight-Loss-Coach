package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class WeightHistoryLogicTest {

    private val zone: ZoneId = ZoneId.of("UTC")
    private val today: LocalDate = LocalDate.of(2026, 8, 28)

    private fun log(kg: Float, date: LocalDate, hour: Int = 12): WeightLog {
        val instant = date.atTime(LocalTime.of(hour, 0)).atZone(zone).toInstant()
        return WeightLog(kg, instant.toEpochMilli())
    }

    @Test
    fun newestEntryIsCurrentWeight() {
        val logs = listOf(
            log(75.8f, LocalDate.of(2026, 8, 20)),
            log(74.2f, LocalDate.of(2026, 8, 28)),
            log(74.9f, LocalDate.of(2026, 8, 23))
        )
        assertEquals(74.2f, WeightHistoryLogic.current(logs)!!.kg, 0.001f)
        assertEquals(LocalDate.of(2026, 8, 28), WeightHistoryLogic.current(logs)!!.localDate(zone))
    }

    @Test
    fun emptyHistoryHasNoCurrentWeight() {
        assertNull(WeightHistoryLogic.current(emptyList()))
    }

    @Test
    fun sevenDayWindowSelectsOnlyLastSevenCalendarDays() {
        val logs = listOf(
            log(80.0f, LocalDate.of(2026, 8, 20)),
            log(75.8f, LocalDate.of(2026, 8, 21)),
            log(75.5f, LocalDate.of(2026, 8, 22)),
            log(74.2f, today)
        )
        val points = WeightHistoryLogic.sevenDayPoints(logs, today, zone)
        assertEquals(listOf(75.5f, 74.2f), points.map { it.kg })
        assertEquals(listOf(LocalDate.of(2026, 8, 22), today), points.map { it.date })
        assertEquals(listOf(0, 6), points.map { it.dayIndex })
    }

    @Test
    fun missingDaysAreNotFabricated() {
        val logs = listOf(
            log(75.5f, LocalDate.of(2026, 8, 22)),
            log(74.2f, today)
        )
        val points = WeightHistoryLogic.sevenDayPoints(logs, today, zone)
        assertEquals(2, points.size)
        val occupied = points.map { it.date }.toSet()
        assertTrue(LocalDate.of(2026, 8, 23) !in occupied)
        assertTrue(LocalDate.of(2026, 8, 24) !in occupied)
        assertTrue(LocalDate.of(2026, 8, 25) !in occupied)
        assertTrue(LocalDate.of(2026, 8, 26) !in occupied)
        assertTrue(LocalDate.of(2026, 8, 27) !in occupied)
    }

    @Test
    fun emptyHistoryYieldsEmptySevenDayPoints() {
        assertTrue(WeightHistoryLogic.sevenDayPoints(emptyList(), today, zone).isEmpty())
    }

    @Test
    fun sevenDayWindowIsSevenCalendarDatesEndingToday() {
        assertEquals(
            listOf(
                LocalDate.of(2026, 8, 22),
                LocalDate.of(2026, 8, 23),
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 26),
                LocalDate.of(2026, 8, 27),
                LocalDate.of(2026, 8, 28)
            ),
            WeightHistoryLogic.sevenDayWindow(today)
        )
    }

    @Test
    fun deltaVsPreviousAvailableLogNotMissingDay() {
        val older = log(74.6f, LocalDate.of(2026, 8, 22))
        val newer = log(74.2f, today)
        val logs = listOf(older, newer)
        assertEquals(-0.4f, WeightHistoryLogic.deltaVsPrevious(logs, newer, zone)!!, 0.001f)
        assertEquals("+0.3 kg", WeightHistoryLogic.formatDeltaKg(0.3f))
        assertEquals("-0.4 kg", WeightHistoryLogic.formatDeltaKg(-0.4f))
        assertEquals(
            "-0.4 kg since previous entry",
            WeightHistoryLogic.formatDeltaLine(-0.4f)
        )
    }

    @Test
    fun firstEverEntryHasNoDelta() {
        val first = log(74.2f, today)
        assertNull(WeightHistoryLogic.deltaVsPrevious(listOf(first), first, zone))
        assertNull(WeightHistoryLogic.formatDeltaLine(null))
        val detail = WeightHistoryLogic.tapDetail(listOf(first), first, zone)
        assertEquals(today, detail.date)
        assertEquals(74.2f, detail.kg, 0.001f)
        assertNull(detail.deltaKg)
    }

    @Test
    fun positiveDeltaFormatsWithPlusSign() {
        val older = log(74.0f, LocalDate.of(2026, 8, 22))
        val newer = log(74.4f, today)
        val delta = WeightHistoryLogic.deltaVsPrevious(listOf(older, newer), newer, zone)!!
        assertEquals(0.4f, delta, 0.001f)
        assertEquals("+0.4 kg", WeightHistoryLogic.formatDeltaKg(delta))
    }

    @Test
    fun sameDaySaveReplacesInsteadOfAppending() {
        val morning = log(75.0f, today, hour = 8)
        val evening = log(74.2f, today, hour = 20)
        val updated = WeightHistoryLogic.upsertForDay(
            logs = listOf(morning),
            kg = evening.kg,
            recordedAtMillis = evening.recordedAtMillis,
            zone = zone
        )
        assertEquals(1, updated.size)
        assertEquals(74.2f, updated.single().kg, 0.001f)
        assertEquals(evening.recordedAtMillis, updated.single().recordedAtMillis)
    }

    @Test
    fun differentDaySaveAppends() {
        val yesterday = log(75.0f, today.minusDays(1))
        val updated = WeightHistoryLogic.upsertForDay(
            logs = listOf(yesterday),
            kg = 74.2f,
            recordedAtMillis = log(74.2f, today).recordedAtMillis,
            zone = zone
        )
        assertEquals(2, updated.size)
        assertEquals(74.2f, WeightHistoryLogic.current(updated)!!.kg, 0.001f)
    }

    @Test
    fun migrateLegacyWhenHistoryEmpty() {
        val migrated = WeightHistoryLogic.migrateLegacyIfEmpty(
            history = emptyList(),
            legacyKg = 74.2f,
            legacyDate = LocalDate.of(2026, 8, 20),
            zone = zone
        )
        assertEquals(1, migrated.size)
        assertEquals(74.2f, migrated.single().kg, 0.001f)
        assertEquals(LocalDate.of(2026, 8, 20), migrated.single().localDate(zone))
    }

    @Test
    fun migrateDoesNotOverrideExistingHistory() {
        val existing = listOf(log(73.0f, today))
        val migrated = WeightHistoryLogic.migrateLegacyIfEmpty(
            history = existing,
            legacyKg = 80.0f,
            legacyDate = LocalDate.of(2026, 8, 1),
            zone = zone
        )
        assertEquals(1, migrated.size)
        assertEquals(73.0f, migrated.single().kg, 0.001f)
    }

    @Test
    fun migrateEmptyWhenNoLegacyEither() {
        assertTrue(
            WeightHistoryLogic.migrateLegacyIfEmpty(emptyList(), null, null, zone).isEmpty()
        )
        assertTrue(
            WeightHistoryLogic.migrateLegacyIfEmpty(emptyList(), -1f, today, zone).isEmpty()
        )
    }

    @Test
    fun formatDateAndKgMatchTapDetailShape() {
        assertEquals("Aug 28", WeightHistoryLogic.formatDateLabel(today))
        assertEquals("74.2 kg", WeightHistoryLogic.formatKg(74.2f))
    }

    @Test
    fun sevenDayDateLabelsAreEnglishMonthDayEnding28Aug2026() {
        assertEquals(
            listOf("Aug 22", "Aug 23", "Aug 24", "Aug 25", "Aug 26", "Aug 27", "Aug 28"),
            WeightHistoryLogic.sevenDayDateLabels(today)
        )
    }

    @Test
    fun sevenDayDateLabelsCrossMonthBoundaryEnding3Sep2026() {
        val end = LocalDate.of(2026, 9, 3)
        assertEquals(
            listOf("Aug 28", "Aug 29", "Aug 30", "Aug 31", "Sep 1", "Sep 2", "Sep 3"),
            WeightHistoryLogic.sevenDayDateLabels(end)
        )
        // Still a rolling last-7 window, not month-start (would be Sep 1..3 only).
        assertEquals(7, WeightHistoryLogic.sevenDayWindow(end).size)
        assertEquals(LocalDate.of(2026, 8, 28), WeightHistoryLogic.sevenDayWindow(end).first())
    }

    @Test
    fun missingStartOrGoalDoesNotInventRemainingOrProgress() {
        assertNull(WeightHistoryLogic.remainingKg(74.2f, null, 70f))
        assertNull(WeightHistoryLogic.remainingKg(74.2f, 80f, null))
        assertNull(WeightHistoryLogic.remainingKg(null, 80f, 70f))
        assertNull(WeightHistoryLogic.progressToGoal(74.2f, 80f, null))
        assertNull(WeightHistoryLogic.progressToGoal(74.2f, null, 70f))
        assertNull(WeightHistoryLogic.progressToGoal(null, 80f, 70f))
        assertEquals("—", WeightHistoryLogic.formatOptionalKg(null))
        assertEquals("—", WeightHistoryLogic.formatProgressPercent(null))
    }

    @Test
    fun remainingAndProgressUseOnlyRealStartGoalCurrent() {
        assertEquals(4.2f, WeightHistoryLogic.remainingKg(74.2f, 80f, 70f)!!, 0.001f)
        assertEquals(0.58f, WeightHistoryLogic.progressToGoal(74.2f, 80f, 70f)!!, 0.01f)
        assertEquals("74.2", WeightHistoryLogic.formatOptionalKg(74.2f))
        assertEquals("58%", WeightHistoryLogic.formatProgressPercent(0.58f))
        assertEquals("0.0", WeightHistoryLogic.formatOptionalKg(0f))
    }

    @Test
    fun zeroPointsMeansNoPlottedPointsAndNoSegments() {
        val points = WeightHistoryLogic.sevenDayPoints(emptyList(), today, zone)
        assertEquals(0, WeightHistoryLogic.plottedPointCount(points))
        assertTrue(WeightHistoryLogic.trendSegments(points).isEmpty())
    }

    @Test
    fun oneRealPointPlotsOneDotAndNoSegment() {
        val logs = listOf(log(74.2f, today))
        val points = WeightHistoryLogic.sevenDayPoints(logs, today, zone)
        assertEquals(1, WeightHistoryLogic.plottedPointCount(points))
        assertEquals(74.2f, points.single().kg, 0.001f)
        assertEquals(today, points.single().date)
        assertTrue(WeightHistoryLogic.trendSegments(points).isEmpty())
    }

    @Test
    fun twoPlusRealPointsProduceSegmentsOnlyBetweenRealLogs() {
        val logs = listOf(
            log(75.5f, LocalDate.of(2026, 8, 22)),
            log(74.8f, LocalDate.of(2026, 8, 24)),
            log(74.2f, today)
        )
        val points = WeightHistoryLogic.sevenDayPoints(logs, today, zone)
        assertEquals(3, WeightHistoryLogic.plottedPointCount(points))
        assertEquals(listOf(0, 2, 6), points.map { it.dayIndex })
        val segments = WeightHistoryLogic.trendSegments(points)
        assertEquals(2, segments.size)
        assertEquals(75.5f, segments[0].first.kg, 0.001f)
        assertEquals(74.8f, segments[0].second.kg, 0.001f)
        assertEquals(74.8f, segments[1].first.kg, 0.001f)
        assertEquals(74.2f, segments[1].second.kg, 0.001f)
        assertEquals(LocalDate.of(2026, 8, 22), segments[0].first.date)
        assertEquals(LocalDate.of(2026, 8, 24), segments[0].second.date)
        assertEquals(today, segments[1].second.date)
    }
}
