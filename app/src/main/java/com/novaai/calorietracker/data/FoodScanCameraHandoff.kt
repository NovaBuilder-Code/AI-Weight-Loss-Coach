package com.novaai.calorietracker.data

/**
 * Camera-return rules for the Photo Calorie Scanner.
 *
 * Samsung camera apps (including SM-A715F) often return RESULT_CANCELED
 * (`success == false`) after the user taps OK, while still writing the
 * EXTRA_OUTPUT file. A real cancel leaves a missing or 0-byte file.
 */
object FoodScanCameraHandoff {
    const val LOG_TAG = "NovaFoodScan"

    fun shouldAcceptCameraResult(success: Boolean, fileLength: Long): Boolean {
        if (success) return true
        return fileLength > 0L
    }
}
