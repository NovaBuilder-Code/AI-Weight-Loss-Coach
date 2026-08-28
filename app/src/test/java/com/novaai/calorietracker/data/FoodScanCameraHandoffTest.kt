package com.novaai.calorietracker.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodScanCameraHandoffTest {

    @Test
    fun samsungOkWithCanceledResultStillAcceptedWhenFileHasBytes() {
        assertTrue(FoodScanCameraHandoff.shouldAcceptCameraResult(success = false, fileLength = 12_345L))
    }

    @Test
    fun userCancelWithEmptyOrMissingFileIsRejected() {
        assertFalse(FoodScanCameraHandoff.shouldAcceptCameraResult(success = false, fileLength = 0L))
        assertFalse(FoodScanCameraHandoff.shouldAcceptCameraResult(success = false, fileLength = -1L))
    }

    @Test
    fun resultOkIsAcceptedEvenIfLengthNotYetFlushed() {
        assertTrue(FoodScanCameraHandoff.shouldAcceptCameraResult(success = true, fileLength = 0L))
        assertTrue(FoodScanCameraHandoff.shouldAcceptCameraResult(success = true, fileLength = -1L))
        assertTrue(FoodScanCameraHandoff.shouldAcceptCameraResult(success = true, fileLength = 800L))
    }
}
