package com.novaai.calorietracker.data

/** Pure TYPE_STEP_COUNTER delta math. Never invents steps. */
data class StepCounterTick(
    val todaySteps: Int,
    val lastCounter: Long
)

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
        val added = if (counter >= lastCounter) counter - lastCounter else counter
        val next = (safeToday.toLong() + added).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        return StepCounterTick(next, counter)
    }
}
