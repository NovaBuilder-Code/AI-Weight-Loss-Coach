package com.novaai.calorietracker.data

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Small pure formatting helpers for showing the saved onboarding profile
 * on the Profile screen. Units are appended by the UI from localized
 * string resources; these functions only produce numbers/values.
 */
object ProfileDisplay {

    /** "175" or "69.9" — one decimal at most, no trailing ".0". */
    fun formatDecimal(value: Float): String {
        val rounded = (value * 10f).roundToInt() / 10f
        return if (rounded % 1f == 0f) rounded.toInt().toString()
        else String.format(Locale.US, "%.1f", rounded)
    }

    /** Weight value text in the profile's display units (kg or lb). */
    fun weightValue(weightKg: Float, imperial: Boolean): String =
        if (imperial) formatDecimal(UnitConversion.kgToLb(weightKg)) else formatDecimal(weightKg)

    /**
     * Height value text in the profile's display units: plain cm for metric,
     * or "feet inches" for imperial (e.g. "5 9"), letting the UI compose the
     * localized ft/in labels.
     */
    fun heightValue(heightCm: Float, imperial: Boolean): String =
        if (imperial) {
            val (feet, inches) = UnitConversion.cmToFeetInches(heightCm)
            "$feet $inches"
        } else {
            formatDecimal(heightCm)
        }

    /** Localized-number-safe grouping for the daily step goal (e.g. "10,000"). */
    fun stepGoalText(goal: Int): String = "%,d".format(goal)
}