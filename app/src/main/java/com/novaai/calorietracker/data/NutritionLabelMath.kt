package com.novaai.calorietracker.data

import kotlin.math.floor

/** Printed per-100 g / per-100 ml nutrition from a readable package label. */
data class NutritionPer100(
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)

/** Portion-scaled macros computed from a per-100g label. */
data class ScaledNutrition(
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)

/**
 * Deterministic conversion from a packaged nutrition label. The model is not
 * trusted to multiply; callers recompute whenever [SOURCE_NUTRITION_LABEL]
 * is paired with per-100 values and a portion in grams.
 */
object NutritionLabelMath {

    const val SOURCE_NUTRITION_LABEL = "nutrition_label"
    const val SOURCE_AI_ESTIMATE = "ai_estimate"

    fun normalizeSource(raw: String?): String =
        when (raw?.trim()?.lowercase()) {
            SOURCE_NUTRITION_LABEL -> SOURCE_NUTRITION_LABEL
            SOURCE_AI_ESTIMATE -> SOURCE_AI_ESTIMATE
            else -> SOURCE_AI_ESTIMATE
        }

    fun scalePer100g(per100: NutritionPer100, portionGrams: Double): ScaledNutrition? {
        if (!portionGrams.isFinite() || portionGrams <= 0.0) return null
        if (!per100.calories.isFinite() || per100.calories < 0.0) return null
        val factor = portionGrams / 100.0
        return ScaledNutrition(
            calories = Math.round(per100.calories.coerceAtLeast(0.0) * factor).toInt(),
            proteinG = round1(per100.proteinG.coerceAtLeast(0.0) * factor),
            carbsG = round1(per100.carbsG.coerceAtLeast(0.0) * factor),
            fatG = round1(per100.fatG.coerceAtLeast(0.0) * factor)
        )
    }

    fun applyScaledToFoods(
        foods: List<FoodItem>,
        scaled: ScaledNutrition,
        portionGrams: Double
    ): List<FoodItem> {
        val portionLabel = portionLabel(portionGrams)
        if (foods.isEmpty()) {
            return listOf(
                FoodItem(
                    name = "food",
                    estimatedPortion = portionLabel,
                    calories = scaled.calories,
                    proteinG = scaled.proteinG,
                    carbsG = scaled.carbsG,
                    fatG = scaled.fatG
                )
            )
        }
        val first = foods.first()
        val updated = first.copy(
            estimatedPortion = first.estimatedPortion.ifEmpty { portionLabel },
            calories = scaled.calories,
            proteinG = scaled.proteinG,
            carbsG = scaled.carbsG,
            fatG = scaled.fatG
        )
        return listOf(updated) + foods.drop(1)
    }

    fun portionLabel(grams: Double): String {
        val text = if (grams == floor(grams) && grams.isFinite()) {
            grams.toInt().toString()
        } else {
            grams.toString().trimEnd('0').trimEnd('.')
        }
        return "$text g"
    }

    private fun round1(v: Double): Double = Math.round(v * 10.0 + 1e-9) / 10.0
}