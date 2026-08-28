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
    fun defaultDisclaimerIsTheRequiredUserFacingText() {
        assertEquals(
            "AI estimates may be inaccurate. Adjust portions or values if needed.",
            FoodScanEdit.DISCLAIMER
        )
        assertEquals(FoodScanEdit.DISCLAIMER, FoodScanJson.DEFAULT_DISCLAIMER)
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

    private val chicken = FoodItem("grilled chicken", "150 g", 250, 40.0, 0.0, 8.0)

    private fun originalResult(foods: List<FoodItem>, total: Int) = FoodScanResult(
        foods = foods,
        totalCalories = total,
        confidence = "medium",
        disclaimer = "stale AI disclaimer"
    )

    @Test
    fun parseDraftAcceptsValidNutrition() {
        val item = FoodScanEdit.parseDraft(FoodEditDraft.from(chicken))
        assertEquals(chicken, item)
    }

    @Test
    fun parseDraftTrimsNameAndPortion() {
        val item = FoodScanEdit.parseDraft(
            FoodEditDraft("  chicken  ", "  1 cup  ", "100", "10", "5", "2")
        )
        assertEquals("chicken", item!!.name)
        assertEquals("1 cup", item.estimatedPortion)
        assertEquals(100, item.calories)
    }

    @Test
    fun parseDraftRejectsBlankName() {
        assertNull(FoodScanEdit.parseDraft(FoodEditDraft.from(chicken).copy(name = "   ")))
        assertNull(FoodScanEdit.parseDraft(FoodEditDraft.from(chicken).copy(name = "")))
    }

    @Test
    fun parseDraftRejectsNegativeCalories() {
        assertNull(FoodScanEdit.parseDraft(FoodEditDraft.from(chicken).copy(calories = "-1")))
    }

    @Test
    fun parseDraftRejectsNegativeMacros() {
        assertNull(FoodScanEdit.parseDraft(FoodEditDraft.from(chicken).copy(proteinG = "-0.1")))
        assertNull(FoodScanEdit.parseDraft(FoodEditDraft.from(chicken).copy(carbsG = "-1")))
        assertNull(FoodScanEdit.parseDraft(FoodEditDraft.from(chicken).copy(fatG = "-8")))
    }

    @Test
    fun parseDraftRejectsNonNumericValues() {
        assertNull(FoodScanEdit.parseDraft(FoodEditDraft.from(chicken).copy(calories = "abc")))
        assertNull(FoodScanEdit.parseDraft(FoodEditDraft.from(chicken).copy(proteinG = "")))
        assertNull(FoodScanEdit.parseDraft(FoodEditDraft.from(chicken).copy(carbsG = "n/a")))
    }

    @Test
    fun parseDraftAllowsZeroCaloriesAndMacros() {
        val item = FoodScanEdit.parseDraft(
            FoodEditDraft("water", "1 glass", "0", "0", "0", "0")
        )
        assertEquals(0, item!!.calories)
        assertEquals(0.0, item.proteinG, 0.0)
        assertEquals(0.0, item.carbsG, 0.0)
        assertEquals(0.0, item.fatG, 0.0)
    }

    @Test
    fun resultFromDraftsSumsEditedCaloriesNotStaleScanTotal() {
        val original = originalResult(
            listOf(
                FoodItem("salad", "1 bowl", 120, 4.0, 10.0, 2.0),
                FoodItem("soup", "1 cup", 180, 8.0, 20.0, 4.0)
            ),
            total = 999
        )
        val drafts = listOf(
            FoodEditDraft("salad", "1 bowl", "150", "5", "12", "3"),
            FoodEditDraft("soup", "1 cup", "200", "9", "22", "5")
        )
        val edited = FoodScanEdit.resultFromDrafts(original, drafts)!!
        assertEquals(350, edited.totalCalories)
        assertEquals(150, edited.foods[0].calories)
        assertEquals(200, edited.foods[1].calories)
        assertEquals(5.0, edited.foods[0].proteinG, 0.0)
        assertEquals(FoodScanEdit.DISCLAIMER, edited.disclaimer)
        assertEquals("medium", edited.confidence)
    }

    @Test
    fun resultFromDraftsReturnsNullWhenAnyItemInvalid() {
        val original = originalResult(listOf(chicken), 250)
        val drafts = listOf(FoodEditDraft.from(chicken).copy(name = ""))
        assertNull(FoodScanEdit.resultFromDrafts(original, drafts))
    }

    @Test
    fun parseAllRejectsEmptyFoodsListOnlyWhenADraftIsInvalid() {
        assertEquals(emptyList<FoodItem>(), FoodScanEdit.parseAll(emptyList()))
        assertNull(FoodScanEdit.parseAll(listOf(FoodEditDraft.from(chicken).copy(calories = "-5"))))
    }
}
