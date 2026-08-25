package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodScanJsonTest {

    @Test
    fun parsesFullValidResponse() {
        val result = FoodScanJson.parseScanResult(
            """{"foods":[{"name":"grilled chicken","estimatedPortion":"150 g","calories":250,"proteinG":40,"carbsG":0,"fatG":8}],"totalCalories":250,"confidence":"medium","disclaimer":"AI estimate — portions and calories may vary."}"""
        )
        assertEquals(1, result!!.foods.size)
        val food = result.foods[0]
        assertEquals("grilled chicken", food.name)
        assertEquals("150 g", food.estimatedPortion)
        assertEquals(250, food.calories)
        assertEquals(40.0, food.proteinG, 0.0)
        assertEquals(0.0, food.carbsG, 0.0)
        assertEquals(8.0, food.fatG, 0.0)
        assertEquals(250, result.totalCalories)
        assertEquals("medium", result.confidence)
        assertEquals("AI estimate — portions and calories may vary.", result.disclaimer)
    }

    @Test
    fun parsesMultipleFoods() {
        val result = FoodScanJson.parseScanResult(
            """{"foods":[{"name":"salad","calories":120},{"name":"soup","calories":180}],"totalCalories":300}"""
        )
        assertEquals(2, result!!.foods.size)
        assertEquals(300, result.totalCalories)
    }

    @Test
    fun noFoodDetectedReturnsEmptyList() {
        val result = FoodScanJson.parseScanResult(
            """{"foods":[],"totalCalories":0,"confidence":"low","disclaimer":"AI estimate — portions and calories may vary."}"""
        )
        assertTrue(result!!.foods.isEmpty())
        assertEquals(0, result.totalCalories)
        assertEquals("low", result.confidence)
    }

    @Test
    fun missingFieldsFallBackToDefaults() {
        val result = FoodScanJson.parseScanResult("""{"foods":[{"name":"pizza"}]}""")
        assertEquals("pizza", result!!.foods[0].name)
        assertEquals("", result.foods[0].estimatedPortion)
        assertEquals(0, result.foods[0].calories)
        assertEquals(0.0, result.foods[0].proteinG, 0.0)
        assertEquals(0.0, result.foods[0].carbsG, 0.0)
        assertEquals(0.0, result.foods[0].fatG, 0.0)
        assertEquals(0, result.totalCalories)
        assertEquals("medium", result.confidence)
        assertEquals(FoodScanJson.DEFAULT_DISCLAIMER, result.disclaimer)
    }

    @Test
    fun totalCaloriesFallsBackToSumOfFoods() {
        val result = FoodScanJson.parseScanResult(
            """{"foods":[{"name":"a","calories":100},{"name":"b","calories":50}],"confidence":"high"}"""
        )
        assertEquals(150, result!!.totalCalories)
        assertEquals("high", result.confidence)
    }

    @Test
    fun negativeValuesAreClampedToZero() {
        val result = FoodScanJson.parseScanResult(
            """{"foods":[{"name":"a","calories":-5,"proteinG":-3}],"totalCalories":-10}"""
        )
        assertEquals(0, result!!.foods[0].calories)
        assertEquals(0.0, result.foods[0].proteinG, 0.0)
        assertEquals(0, result.totalCalories)
    }

    @Test
    fun moreThanTenFoodsAreCapped() {
        val many = (1..15).joinToString(",") { """{"name":"food $it","calories":1}""" }
        val result = FoodScanJson.parseScanResult("""{"foods":[$many]}""")
        assertEquals(10, result!!.foods.size)
    }

    @Test
    fun handlesEscapedQuotesAndUnicodeInName() {
        val result = FoodScanJson.parseScanResult(
            """{"foods":[{"name":"chicken \"BBQ\" \u2605","calories":1}]}"""
        )
        assertEquals("chicken \"BBQ\" ★", result!!.foods[0].name)
    }

    @Test
    fun rejectsGarbageText() {
        assertNull(FoodScanJson.parseScanResult("not json at all"))
        assertNull(FoodScanJson.parseScanResult(""))
        assertNull(FoodScanJson.parseScanResult("""{"foods":""""))
    }

    @Test
    fun rejectsNonObjectJson() {
        assertNull(FoodScanJson.parseScanResult("""[1,2,3]"""))
        assertNull(FoodScanJson.parseScanResult(""""hello""""))
        assertNull(FoodScanJson.parseScanResult("""42"""))
    }

    @Test
    fun rejectsMarkdownFencedJson() {
        // The Worker strips fences before replying; fenced JSON reaching the
        // app is treated as malformed (server error path), never parsed.
        assertNull(
            FoodScanJson.parseScanResult(
                "```json\n{\"foods\":[{\"name\":\"apple\",\"calories\":95}],\"totalCalories\":95}\n```"
            )
        )
    }

    @Test
    fun parseReturnsMapsAndLists() {
        val value = FoodScanJson.parse("""{"a":1,"b":[true,false,null],"c":"x"}""") as Map<*, *>
        assertEquals(1.0, value["a"] as Double, 0.0)
        val list = value["b"] as List<*>
        assertEquals(true, list[0])
        assertEquals(false, list[1])
        assertEquals(null, list[2])
        assertEquals("x", value["c"])
    }
}