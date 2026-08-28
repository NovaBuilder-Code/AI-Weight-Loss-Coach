package com.novaai.calorietracker.data

import java.time.Instant
import java.time.ZonedDateTime

/** Why today's Health Connect step read produced the displayed count. */
enum class TodayStepsStatus {
    /** Permission granted and the aggregate call succeeded. Zero is a real empty day. */
    OK,
    /** Health Connect is present but READ_STEPS is not granted. */
    PERMISSION_REQUIRED,
    /** Health Connect is not available on this device. */
    UNAVAILABLE,
    /** Health Connect is installed but needs a provider update. */
    UPDATE_REQUIRED,
    /** Granted (or unknown) but the read threw; fail closed. */
    ERROR
}

enum class HealthConnectAvailability {
    AVAILABLE,
    UNAVAILABLE,
    UPDATE_REQUIRED
}

data class TodayStepsRead(
    val steps: Int,
    val status: TodayStepsStatus
)

/**
 * Pure mapping for today's step total from Health Connect.
 * Fail closed: never invent a count; missing/denied/error is 0.
 */
object HealthConnectSteps {

    /**
     * Device-timezone window from local midnight today up to [now].
     * Pair is (startInclusive, endExclusive-or-now).
     */
    fun todayRange(now: ZonedDateTime): Pair<Instant, Instant> {
        val start = now.toLocalDate().atStartOfDay(now.zone).toInstant()
        return start to now.toInstant()
    }

    fun stepsFromAggregate(count: Long?): Int {
        if (count == null || count <= 0L) return 0
        return count.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    fun result(
        availability: HealthConnectAvailability,
        permissionGranted: Boolean,
        aggregateCount: Long? = null,
        readError: Boolean = false
    ): TodayStepsRead {
        when (availability) {
            HealthConnectAvailability.UNAVAILABLE ->
                return TodayStepsRead(0, TodayStepsStatus.UNAVAILABLE)
            HealthConnectAvailability.UPDATE_REQUIRED ->
                return TodayStepsRead(0, TodayStepsStatus.UPDATE_REQUIRED)
            HealthConnectAvailability.AVAILABLE -> Unit
        }
        if (!permissionGranted) {
            return TodayStepsRead(0, TodayStepsStatus.PERMISSION_REQUIRED)
        }
        if (readError) {
            return TodayStepsRead(0, TodayStepsStatus.ERROR)
        }
        return TodayStepsRead(stepsFromAggregate(aggregateCount), TodayStepsStatus.OK)
    }
}