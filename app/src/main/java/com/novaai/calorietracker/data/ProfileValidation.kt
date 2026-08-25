package com.novaai.calorietracker.data

import com.novaai.calorietracker.ui.screens.onboarding.MAX_AGE
import com.novaai.calorietracker.ui.screens.onboarding.MAX_HEIGHT_CM
import com.novaai.calorietracker.ui.screens.onboarding.MAX_STEP_GOAL
import com.novaai.calorietracker.ui.screens.onboarding.MAX_WEIGHT_KG
import com.novaai.calorietracker.ui.screens.onboarding.MIN_AGE
import com.novaai.calorietracker.ui.screens.onboarding.MIN_HEIGHT_CM
import com.novaai.calorietracker.ui.screens.onboarding.MIN_STEP_GOAL
import com.novaai.calorietracker.ui.screens.onboarding.MIN_WEIGHT_KG

/**
 * Pure validation for the profile edit form. Uses the exact same ranges as
 * the onboarding questionnaire (the constants live in ProfileSetupScreen.kt)
 * so edits can never accept values onboarding would reject.
 */
object ProfileValidation {

    fun validName(name: String): Boolean = name.trim().isNotEmpty()

    fun validAge(age: Int): Boolean = age in MIN_AGE..MAX_AGE

    fun validHeightCm(cm: Float): Boolean = cm in MIN_HEIGHT_CM..MAX_HEIGHT_CM

    fun validWeightKg(kg: Float): Boolean = kg in MIN_WEIGHT_KG..MAX_WEIGHT_KG

    fun validStepGoal(goal: Int): Boolean = goal in MIN_STEP_GOAL..MAX_STEP_GOAL
}