package com.novaai.calorietracker.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/** One persisted weight log: kilograms at a calendar date/time (epoch millis). */
data class WeightLog(
    val kg: Float,
    val recordedAtMillis: Long
) {
    fun localDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(recordedAtMillis).atZone(zone).toLocalDate()
}

/** A real plotted point in the last-7-calendar-day window. Missing days are omitted. */
data class WeightChartPoint(
    val date: LocalDate,
    val kg: Float,
    val dayIndex: Int,
    val log: WeightLog
)

data class WeightTapDetail(
    val date: LocalDate,
    val kg: Float,
    val deltaKg: Float?
)

/**
 * Pure, Android-free weight-history decisions so the 7-day trend, deltas, and
 * same-day replace rules can be unit-tested on the JVM.
 */
object WeightHistoryLogic {

    private val dateLabelFmt = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

    fun sortedChronological(logs: List<WeightLog>): List<WeightLog> =
        logs.sortedBy { it.recordedAtMillis }

    /** Newest persisted entry is the current weight. */
    fun current(logs: List<WeightLog>): WeightLog? =
        sortedChronological(logs).lastOrNull()

    /**
     * Keeps one log per calendar day (the latest that day) so a second save on
     * the same day replaces rather than inventing extra days.
     */
    fun onePerDay(logs: List<WeightLog>, zone: ZoneId = ZoneId.systemDefault()): List<WeightLog> =
        logs.groupBy { it.localDate(zone) }
            .map { (_, dayLogs) -> dayLogs.maxBy { it.recordedAtMillis } }
            .sortedBy { it.recordedAtMillis }

    /**
     * If [day] already has an entry, replace it in place. Otherwise append.
     * Matches the existing add-weight dialog (today's log is updated, not duplicated).
     */
    fun upsertForDay(
        logs: List<WeightLog>,
        kg: Float,
        recordedAtMillis: Long,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<WeightLog> {
        if (kg <= 0f) return onePerDay(logs, zone)
        val day = Instant.ofEpochMilli(recordedAtMillis).atZone(zone).toLocalDate()
        val withoutThatDay = logs.filter { it.localDate(zone) != day }
        return onePerDay(withoutThatDay + WeightLog(kg, recordedAtMillis), zone)
    }

    /**
     * Real points in the inclusive window `[today-6, today]`. Days with no log
     * are skipped — never fabricated. [dayIndex] is 0 for today-6 and 6 for today
     * so the chart can leave honest gaps on the x-axis.
     */
    fun sevenDayPoints(
        logs: List<WeightLog>,
        today: LocalDate,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<WeightChartPoint> {
        val start = today.minusDays(6)
        val byDay = onePerDay(logs, zone)
            .filter { it.localDate(zone) in start..today }
            .associateBy { it.localDate(zone) }
        return (0L..6L).mapNotNull { offset ->
            val date = start.plusDays(offset)
            val log = byDay[date] ?: return@mapNotNull null
            WeightChartPoint(date = date, kg = log.kg, dayIndex = offset.toInt(), log = log)
        }
    }

    /** The seven calendar dates of the trend window, oldest first. */
    fun sevenDayWindow(today: LocalDate): List<LocalDate> =
        (0L..6L).map { today.minusDays(6 - it) }

    /**
     * Change vs the previous available chronological log (any day — not vs a
     * missing day, and not vs a fabricated 0). Null when this is the first log.
     */
    fun deltaVsPrevious(
        logs: List<WeightLog>,
        selected: WeightLog,
        zone: ZoneId = ZoneId.systemDefault()
    ): Float? {
        val sorted = onePerDay(logs, zone)
        val idx = sorted.indexOfFirst { it.recordedAtMillis == selected.recordedAtMillis }
        if (idx <= 0) return null
        return selected.kg - sorted[idx - 1].kg
    }

    fun tapDetail(
        logs: List<WeightLog>,
        selected: WeightLog,
        zone: ZoneId = ZoneId.systemDefault()
    ): WeightTapDetail = WeightTapDetail(
        date = selected.localDate(zone),
        kg = selected.kg,
        deltaKg = deltaVsPrevious(logs, selected, zone)
    )

    fun formatDateLabel(date: LocalDate): String = date.format(dateLabelFmt)

    fun formatKg(kg: Float): String = String.format(Locale.US, "%.1f kg", kg)

    fun formatDeltaKg(deltaKg: Float): String {
        val mag = String.format(Locale.US, "%.1f", abs(deltaKg))
        return when {
            deltaKg > 0f -> "+$mag kg"
            deltaKg < 0f -> "-$mag kg"
            else -> "0.0 kg"
        }
    }

    fun formatDeltaLine(deltaKg: Float?): String? =
        deltaKg?.let { "${formatDeltaKg(it)} since previous entry" }

    /**
     * If the JSON history is empty but the old single current_weight_kg exists,
     * seed history with that one log so existing users keep their last entry.
     */
    fun migrateLegacyIfEmpty(
        history: List<WeightLog>,
        legacyKg: Float?,
        legacyDate: LocalDate?,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<WeightLog> {
        val existing = onePerDay(history, zone)
        if (existing.isNotEmpty()) return existing
        if (legacyKg == null || legacyKg <= 0f || legacyDate == null) return emptyList()
        val millis = legacyDate.atStartOfDay(zone).toInstant().toEpochMilli()
        return listOf(WeightLog(legacyKg, millis))
    }
}
