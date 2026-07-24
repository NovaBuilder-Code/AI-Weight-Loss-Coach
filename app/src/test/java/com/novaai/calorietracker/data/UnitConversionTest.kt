package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConversionTest {

    @Test
    fun feetInchesConvertToCentimeters() {
        assertEquals(175.26f, UnitConversion.feetInchesToCm(5, 9f), 0.01f)
        assertEquals(182.88f, UnitConversion.feetInchesToCm(6, 0f), 0.01f)
        assertEquals(30.48f, UnitConversion.feetInchesToCm(1, 0f), 0.01f)
    }

    @Test
    fun centimetersConvertToFeetAndInches() {
        assertEquals(Pair(5, 9), UnitConversion.cmToFeetInches(175.26f))
        assertEquals(Pair(6, 0), UnitConversion.cmToFeetInches(182.88f))
        // 182.0 cm ≈ 71.65 in → rounds to 72 in and must carry into 6 ft 0 in.
        assertEquals(Pair(6, 0), UnitConversion.cmToFeetInches(182f))
    }

    @Test
    fun heightRoundTripsWithinAnInch() {
        for (cm in 100..250 step 5) {
            val (ft, inch) = UnitConversion.cmToFeetInches(cm.toFloat())
            val back = UnitConversion.feetInchesToCm(ft, inch.toFloat())
            assertEquals(cm.toFloat(), back, 1.28f) // half an inch rounding, in cm
        }
    }

    @Test
    fun poundsConvertToKilograms() {
        assertEquals(69.85f, UnitConversion.lbToKg(154f), 0.01f)
        assertEquals(45.36f, UnitConversion.lbToKg(100f), 0.01f)
    }

    @Test
    fun kilogramsConvertToPounds() {
        assertEquals(154.32f, UnitConversion.kgToLb(70f), 0.01f)
    }

    @Test
    fun weightRoundTripsExactly() {
        for (kg in 30..300 step 10) {
            val back = UnitConversion.lbToKg(UnitConversion.kgToLb(kg.toFloat()))
            assertEquals(kg.toFloat(), back, 0.001f)
        }
    }
}
