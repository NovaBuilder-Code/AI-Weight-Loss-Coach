package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileDisplayTest {

    @Test
    fun formatDecimalNoTrailingZeroOrPoint() {
        assertEquals("175", ProfileDisplay.formatDecimal(175f))
        assertEquals("70", ProfileDisplay.formatDecimal(70f))
        assertEquals("175.3", ProfileDisplay.formatDecimal(175.26f))
        assertEquals("69.9", ProfileDisplay.formatDecimal(69.853f))
        assertEquals("154", ProfileDisplay.formatDecimal(153.9995f))
    }

    @Test
    fun weightValueUsesMetricOrImperial() {
        assertEquals("70", ProfileDisplay.weightValue(70f, imperial = false))
        assertEquals("69.9", ProfileDisplay.weightValue(69.853f, imperial = false))
        // 69.853 kg ≈ 154 lb after rounding to one decimal.
        assertEquals("154", ProfileDisplay.weightValue(69.853f, imperial = true))
        assertEquals("154.3", ProfileDisplay.weightValue(70f, imperial = true))
    }

    @Test
    fun heightValueUsesMetricOrFeetInches() {
        assertEquals("175.3", ProfileDisplay.heightValue(175.26f, imperial = false))
        assertEquals("5 9", ProfileDisplay.heightValue(175.26f, imperial = true))
        assertEquals("6 0", ProfileDisplay.heightValue(182.88f, imperial = true))
    }

    @Test
    fun stepGoalIsGrouped() {
        assertEquals("10,000", ProfileDisplay.stepGoalText(10_000))
        assertEquals("8,000", ProfileDisplay.stepGoalText(8_000))
    }
}