package com.novaai.calorietracker.data

/** Pure TYPE_STEP_COUNTER delta math. Never invents steps. */
data class StepCounterTick(
    val todaySteps: Int,
    val lastCounter: Long
)

/**
 * In-session merge of TYPE_STEP_DETECTOR (+1 live) and TYPE_STEP_COUNTER
 * (batched hardware total). Published steps are
 * baselineToday + max(detectorSince, counter - baselineCounter)
 * so the two sensors cannot be summed.
 */
data class NativeStepState(
    val todaySteps: Int = 0,
    val lastCounter: Long = StepCounterMath.UNSET_COUNTER,
    val baselineCounter: Long = StepCounterMath.UNSET_COUNTER,
    val detectorSince: Int = 0,
    val baselineToday: Int = 0
) {
    fun counterDelta(): Int {
        if (baselineCounter < 0L || lastCounter < baselineCounter) return 0
        return (lastCounter - baselineCounter).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    fun published(): Int {
        val native = maxOf(detectorSince.coerceAtLeast(0), counterDelta())
        return (baselineToday.toLong() + native).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    }
}

object StepCounterMath {
    const val UNSET_COUNTER = -1L

    /**
     * [counter] is the boot-lifetime hardware count.
     * First sample baselines without adding the lifetime total.
     * If [counter] < [lastCounter], the device rebooted; add [counter]
     * (steps since boot) rather than a negative jump.
     */
    fun onTick(todaySteps: Int, lastCounter: Long, counter: Long): StepCounterTick {
        val safeToday = todaySteps.coerceAtLeast(0)
        if (lastCounter < 0L) {
            return StepCounterTick(safeToday, counter)
        }
        val added = if (counter >= lastCounter) counter - lastCounter else 0L
        val next = (safeToday.toLong() + added).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        return StepCounterTick(next, counter)
    }

    fun onCounter(state: NativeStepState, counter: Long): NativeStepState {
        if (state.baselineCounter < 0L) {
            val last = state.lastCounter
            if (last >= 0L && counter >= last) {
                val catchup = (counter - last).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                val newToday = (state.todaySteps.toLong() + catchup)
                    .coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
                return NativeStepState(
                    todaySteps = newToday,
                    lastCounter = counter,
                    baselineCounter = counter,
                    detectorSince = 0,
                    baselineToday = newToday
                )
            }
            if (last >= 0L && counter < last) {
                // Reboot: hardware count restarted. Do not treat steps-since-boot
                // as today's total.
                return startSession(state.copy(todaySteps = state.todaySteps), counter)
            }
            val seeded = NativeStepState(
                todaySteps = state.todaySteps,
                lastCounter = counter,
                baselineCounter = counter,
                detectorSince = 0,
                baselineToday = state.todaySteps
            )
            return seeded.copy(todaySteps = seeded.published())
        }
        val updated = state.copy(lastCounter = counter)
        return updated.copy(todaySteps = updated.published())
    }

    fun onDetector(state: NativeStepState): NativeStepState {
        val updated = state.copy(detectorSince = state.detectorSince + 1)
        return updated.copy(todaySteps = updated.published())
    }

    /**
     * Start (or recover) a tracking session from the current hardware count.
     * Reboot (counter < last) or the "today == current boot count" corruption
     * from an earlier reboot must not become the displayed total.
     */
    fun startSession(state: NativeStepState, counter: Long): NativeStepState {
        val reboot = state.lastCounter >= 0L && counter < state.lastCounter
        val bootCountCopiedAsToday =
            counter >= 0L &&
                state.todaySteps.toLong() == counter &&
                state.lastCounter == counter &&
                state.detectorSince == 0
        val today = if (reboot || bootCountCopiedAsToday) 0 else state.todaySteps.coerceAtLeast(0)
        return NativeStepState(
            todaySteps = today,
            lastCounter = counter,
            baselineCounter = counter,
            detectorSince = 0,
            baselineToday = today
        )
    }
}
