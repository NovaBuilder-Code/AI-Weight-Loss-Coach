package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NutritionLabelMathTest {

    private val lindahlsPer100 = NutritionPer100(
        calories = 59.0,
        proteinG = 10.0,
        carbsG = 3.3,
        fatG = 0.2
    )

    @Test
    fun scalePer100gLindahls150g() {
        val scaled = NutritionLabelMath.scalePer100g(lindahlsPer100, 150.0)!!
        assertEquals(89, scaled.calories)
        assertEquals(15.0, scaled.proteinG, 0.01)
        assertEquals(5.0, scaled.carbsG, 0.1)
        assertEquals(0.3, scaled.fatG, 0.01)
    }

    @Test
    fun packagedLabelValuesTakePriorityOverConflictingEstimate() {
        val json = """
            {"foods":[{"name":"Lindahls protein quark","estimatedPortion":"150 g",
            "calories":120,"proteinG":8,"carbsG":8,"fatG":4}],
            "totalCalories":120,"confidence":"medium",
            "disclaimer":"AI estimate — portions and calories may vary.",
            "source":"nutrition_label","basis":"per_100g","portionGrams":150,
            "per100":{"calories":59,"proteinG":10,"carbsG":3.3,"fatG":0.2}}
        """.trimIndent()
        val result = FoodScanJson.parseScanResult(json)!!
        assertEquals(NutritionLabelMath.SOURCE_NUTRITION_LABEL, result.source)
        assertEquals(1, result.foods.size)
        val food = result.foods[0]
        assertEquals("Lindahls protein quark", food.name)
        assertEquals("150 g", food.estimatedPortion)
        assertEquals(89, food.calories)
        assertEquals(15.0, food.proteinG, 0.01)
        assertEquals(5.0, food.carbsG, 0.1)
        assertEquals(0.3, food.fatG, 0.01)
        assertEquals(89, result.totalCalories)
        assertEquals(59.0, result.per100!!.calories, 0.0)
        assertEquals(150.0, result.portionGrams!!, 0.0)
        assertEquals("per_100g", result.basis)
    }

    @Test
    fun noReadableLabelParsesAsAiEstimateUsingFoods() {
        val json = """{"foods":[{"name":"grilled chicken","estimatedPortion":"150 g","calories":250,"proteinG":40,"carbsG":0,"fatG":8}],"totalCalories":250,"confidence":"medium","disclaimer":"AI estimate — portions and calories may vary."}"""
        val result = FoodScanJson.parseScanResult(json)!!
        assertEquals(NutritionLabelMath.SOURCE_AI_ESTIMATE, result.source)
        assertNull(result.per100)
        assertNull(result.portionGrams)
        assertEquals(250, result.foods[0].calories)
        assertEquals(40.0, result.foods[0].proteinG, 0.0)
        assertEquals(250, result.totalCalories)
    }

    @Test
    fun explicitAiEstimateIgnoresPer100EvenIfPresent() {
        val json = """{"foods":[{"name":"apple","estimatedPortion":"1 medium","calories":95,"proteinG":0.5,"carbsG":25,"fatG":0.3}],"totalCalories":95,"source":"ai_estimate","portionGrams":150,"per100":{"calories":59,"proteinG":10,"carbsG":3.3,"fatG":0.2}}"""
        val result = FoodScanJson.parseScanResult(json)!!
        assertEquals(NutritionLabelMath.SOURCE_AI_ESTIMATE, result.source)
        assertEquals(95, result.foods[0].calories)
        assertEquals(0.5, result.foods[0].proteinG, 0.0)
        assertEquals(95, result.totalCalories)
    }

    @Test
    fun missingExtraFieldsDoNotCrash() {
        val result = FoodScanJson.parseScanResult("""{"foods":[{"name":"salad","calories":120}]}""")
        assertNotNull(result)
        assertEquals("salad", result!!.foods[0].name)
        assertEquals(120, result.foods[0].calories)
        assertEquals(NutritionLabelMath.SOURCE_AI_ESTIMATE, result.source)
        assertNull(result.per100)
        assertNull(result.portionGrams)
        assertNull(result.basis)
    }

    @Test
    fun malformedExtraFieldsDoNotCrashAndFallBackToFoods() {
        val json = """{"foods":[{"name":"yogurt","estimatedPortion":"150 g","calories":120,"proteinG":8,"carbsG":8,"fatG":4}],"totalCalories":120,"source":"nutrition_label","portionGrams":"not-a-number","per100":"nope","basis":["per_100g"]}"""
        val result = FoodScanJson.parseScanResult(json)!!
        assertEquals(120, result.foods[0].calories)
        assertEquals(8.0, result.foods[0].proteinG, 0.0)
        assertEquals(120, result.totalCalories)
        assertNull(result.per100)
        assertNull(result.portionGrams)
    }

    @Test
    fun malformedPer100ObjectFallsBackToFoods() {
        val json = """{"foods":[{"name":"yogurt","calories":120,"proteinG":8,"carbsG":8,"fatG":4}],"totalCalories":120,"source":"nutrition_label","portionGrams":150,"per100":{"calories":"unreadable","proteinG":true}}"""
        val result = FoodScanJson.parseScanResult(json)!!
        assertEquals(120, result.foods[0].calories)
        assertEquals(8.0, result.foods[0].proteinG, 0.0)
        assertNull(result.per100)
    }

    @Test
    fun unknownSourceFallsBackToAiEstimate() {
        val json = """{"foods":[{"name":"rice","calories":200}],"source":"guesswork"}"""
        val result = FoodScanJson.parseScanResult(json)!!
        assertEquals(NutritionLabelMath.SOURCE_AI_ESTIMATE, result.source)
        assertEquals(200, result.foods[0].calories)
    }

    @Test
    fun scalePer100gRejectsNonPositivePortion() {
        assertNull(NutritionLabelMath.scalePer100g(lindahlsPer100, 0.0))
        assertNull(NutritionLabelMath.scalePer100g(lindahlsPer100, -50.0))
        assertNull(NutritionLabelMath.scalePer100g(lindahlsPer100, Double.NaN))
    }
}