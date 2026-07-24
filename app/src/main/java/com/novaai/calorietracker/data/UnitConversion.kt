package com.novaai.calorietracker.data

import kotlin.math.roundToInt

/**
 * Conversions between metric (the canonical storage format, see UserProfile)
 * and imperial display units used by the onboarding inputs.
 */
object UnitConversion {
    private const val CM_PER_INCH = 2.54f
    private const val LB_PER_KG = 2.2046226f

    fun feetInchesToCm(feet: Int, inches: Float): Float =
        (feet * 12 + inches) * CM_PER_INCH

    /** Nearest whole inch, carried into feet when it rounds up to 12. */
    fun cmToFeetInches(cm: Float): Pair<Int, Int> {
        val totalInches = (cm / CM_PER_INCH).roundToInt()
        return Pair(totalInches / 12, totalInches % 12)
    }

    fun lbToKg(lb: Float): Float = lb / LB_PER_KG

    fun kgToLb(kg: Float): Float = kg * LB_PER_KG
}
