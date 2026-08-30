package com.novaai.calorietracker.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log

object StepCounterHardware {
    private const val TAG = "NovaHealthConnect"

    fun hasStepCounter(context: Context): Boolean =
        sensor(context, Sensor.TYPE_STEP_COUNTER) != null

    fun hasStepDetector(context: Context): Boolean =
        sensor(context, Sensor.TYPE_STEP_DETECTOR) != null

    fun stepCounter(context: Context): Sensor? =
        sensor(context, Sensor.TYPE_STEP_COUNTER)

    fun stepDetector(context: Context): Sensor? =
        sensor(context, Sensor.TYPE_STEP_DETECTOR)

    fun describe(sensor: Sensor?): String {
        if (sensor == null) return "null"
        return "name=${sensor.name} vendor=${sensor.vendor} version=${sensor.version} " +
            "type=${sensor.type} wakeup=${sensor.isWakeUpSensor} " +
            "fifoMax=${sensor.fifoMaxEventCount} fifoRes=${sensor.fifoReservedEventCount} " +
            "minDelay=${sensor.minDelay} maxDelay=${sensor.maxDelay} " +
            "reportingMode=${sensor.reportingMode}"
    }

    fun logAvailability(context: Context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val counter = sm?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val counterWake = sm?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER, true)
        val detector = sm?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val detectorWake = sm?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR, true)
        Log.i(
            TAG,
            "step_hardware counter=${counter != null} detector=${detector != null} " +
                "counterWake=${counterWake != null} detectorWake=${detectorWake != null} " +
                "counter=${describe(counter)} detector=${describe(detector)}"
        )
    }

    private fun sensor(context: Context, type: Int): Sensor? {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        return sm?.getDefaultSensor(type)
    }
}
