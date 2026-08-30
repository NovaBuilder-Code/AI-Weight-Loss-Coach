package com.novaai.calorietracker.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

/**
 * Local cache of today's step total (Health Connect when it has records,
 * otherwise the on-device TYPE_STEP_COUNTER / TYPE_STEP_DETECTOR merge)
 * used by Walking Tracker (and the Home steps tile).
 * This store never invents demo/sample steps. A previous day's cache starts
 * the new day at zero. The hardware counter baseline is kept across days.
 */
object StepsStore {
    private const val PREFS_NAME = "steps_tracker"
    private const val KEY_STEPS = "today_steps"
    private const val KEY_STEPS_DATE = "steps_date"
    private const val KEY_LAST_COUNTER = "last_step_counter"
    private const val KEY_BASELINE_COUNTER = "baseline_step_counter"
    private const val KEY_DETECTOR_SINCE = "detector_since_baseline"
    private const val KEY_BASELINE_TODAY = "baseline_today_steps"
    private const val DEFAULT_STEPS = 0

    private val lock = Any()
    private var memory: NativeStepState? = null
    private var memoryDate: String? = null

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun todayString(): String = LocalDate.now().toString()

    private fun rolloverIfNeeded(p: SharedPreferences) {
        val today = todayString()
        val savedDate = p.getString(KEY_STEPS_DATE, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (savedDate != null && savedDate.isBefore(LocalDate.now())) {
            p.edit()
                .putInt(KEY_STEPS, 0)
                .putString(KEY_STEPS_DATE, today)
                .putInt(KEY_DETECTOR_SINCE, 0)
                .putInt(KEY_BASELINE_TODAY, 0)
                .putLong(KEY_BASELINE_COUNTER, StepCounterMath.UNSET_COUNTER)
                .apply()
            memory = null
            memoryDate = null
        }
    }

    private fun readState(context: Context): NativeStepState {
        val today = todayString()
        if (memory != null && memoryDate == today) return memory!!
        val p = prefs(context)
        rolloverIfNeeded(p)
        val state = NativeStepState(
            todaySteps = p.getInt(KEY_STEPS, DEFAULT_STEPS),
            lastCounter = p.getLong(KEY_LAST_COUNTER, StepCounterMath.UNSET_COUNTER),
            baselineCounter = p.getLong(KEY_BASELINE_COUNTER, StepCounterMath.UNSET_COUNTER),
            detectorSince = p.getInt(KEY_DETECTOR_SINCE, 0),
            baselineToday = p.getInt(KEY_BASELINE_TODAY, 0)
        )
        memory = state
        memoryDate = today
        return state
    }

    private fun writeState(context: Context, state: NativeStepState): NativeStepState {
        memory = state
        memoryDate = todayString()
        prefs(context).edit()
            .putInt(KEY_STEPS, state.todaySteps)
            .putString(KEY_STEPS_DATE, memoryDate)
            .putLong(KEY_LAST_COUNTER, state.lastCounter)
            .putLong(KEY_BASELINE_COUNTER, state.baselineCounter)
            .putInt(KEY_DETECTOR_SINCE, state.detectorSince)
            .putInt(KEY_BASELINE_TODAY, state.baselineToday)
            .apply()
        return state
    }

    /**
     * Applies the day-rollover rule (a previous day's count starts the new
     * day at zero) and returns today's cached step count.
     * Does not reset the hardware counter baseline across days except
     * clearing the in-session detector merge.
     */
    fun loadToday(context: Context): Int = synchronized(lock) {
        readState(context).todaySteps
    }

    fun save(context: Context, steps: Int) {
        synchronized(lock) {
            val cur = readState(context)
            writeState(context, cur.copy(todaySteps = steps, baselineToday = steps, detectorSince = 0))
        }
    }

    fun lastCounter(context: Context): Long = synchronized(lock) {
        readState(context).lastCounter
    }

    fun snapshot(context: Context): NativeStepState = synchronized(lock) {
        readState(context)
    }

    fun startTrackingSession(context: Context, counter: Long): NativeStepState = synchronized(lock) {
        writeState(context, StepCounterMath.startSession(readState(context), counter))
    }

    fun applyCounterEvent(context: Context, counter: Long): NativeStepState = synchronized(lock) {
        writeState(context, StepCounterMath.onCounter(readState(context), counter))
    }

    fun addDetectedStep(context: Context): NativeStepState = synchronized(lock) {
        writeState(context, StepCounterMath.onDetector(readState(context)))
    }
}
