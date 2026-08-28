package com.novaai.calorietracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieStoreLogTest {

    private val chicken = FoodItem("grilled chicken", "150 g", 250, 40.0, 0.0, 8.0)

    private fun scanResult(foods: List<FoodItem>, total: Int) = FoodScanResult(
        foods = foods,
        totalCalories = total,
        confidence = "medium",
        disclaimer = "AI estimate — portions and calories may vary."
    )

    @Test
    fun mealsFromScanMapsEachFoodWithOwnCaloriesAndDate() {
        val meals = CalorieStore.mealsFromScan(scanResult(listOf(chicken), 250), "2026-08-27")
        assertEquals(1, meals.size)
        val meal = meals[0]
        assertEquals("grilled chicken", meal.name)
        assertEquals(250, meal.kcal)
        assertEquals("2026-08-27", meal.date)
    }

    @Test
    fun mealsFromScanUsesPerFoodCaloriesNotScanTotal() {
        val multi = scanResult(
            listOf(FoodItem("salad", "", 120, 0.0, 0.0, 0.0), FoodItem("soup", "", 180, 0.0, 0.0, 0.0)),
            total = 999
        )
        val meals = CalorieStore.mealsFromScan(multi, "2026-08-27")
        assertEquals(listOf(120, 180), meals.map { it.kcal })
    }

    @Test
    fun mealsFromScanEmptyFoodsYieldsEmptyList() {
        assertTrue(CalorieStore.mealsFromScan(scanResult(emptyList(), 0), "2026-08-27").isEmpty())
    }

    @Test
    fun mealsFromScanPreservesMacros() {
        val meals = CalorieStore.mealsFromScan(scanResult(listOf(chicken), 250), "2026-08-27")
        val meal = meals[0]
        assertEquals(40.0, meal.proteinG, 0.0)
        assertEquals(0.0, meal.carbsG, 0.0)
        assertEquals(8.0, meal.fatG, 0.0)
    }

    @Test
    fun mealsFromScanDefaultsToToday() {
        val today = java.time.LocalDate.now().toString()
        assertEquals(today, CalorieStore.mealsFromScan(scanResult(listOf(chicken), 250))[0].date)
    }

    @Test
    fun formatMacrosBuildsLineWithValues() {
        assertEquals(
            "40 g protein · 0 g carbs · 8 g fat",
            CalorieStore.formatMacros(40.0, 0.0, 8.0)
        )
    }

    @Test
    fun formatMacrosTrimsDecimalTrailingZeros() {
        assertEquals("12.5 g protein · 3 g carbs · 0.25 g fat", CalorieStore.formatMacros(12.5, 3.0, 0.25))
    }

    @Test
    fun formatMacrosEmptyWhenAllZero() {
        assertEquals("", CalorieStore.formatMacros(0.0, 0.0, 0.0))
    }

    @Test
    fun appendLoggedFoodsAddsNewMealsInOrder() {
        val existing = listOf(StoredMeal("apple", 80, "2026-08-27"))
        val new = listOf(StoredMeal("banana", 100, "2026-08-27"))
        val updated = CalorieStore.appendLoggedFoods(existing, new)
        assertEquals(listOf("apple", "banana"), updated.map { it.name })
        assertEquals(2, updated.size)
    }

    @Test
    fun appendLoggedFoodsSkipsExactDuplicate() {
        val meal = StoredMeal("grilled chicken", 250, "2026-08-27")
        val updated = CalorieStore.appendLoggedFoods(listOf(meal), listOf(meal))
        assertEquals(1, updated.size)
    }

    @Test
    fun appendLoggedFoodsKeepsExistingMealsUntouched() {
        val existing = listOf(StoredMeal("apple", 80, "2026-08-27"))
        val updated = CalorieStore.appendLoggedFoods(existing, listOf(StoredMeal("chicken", 250, "2026-08-27")))
        assertEquals("apple", updated[0].name)
        assertEquals("chicken", updated[1].name)
    }

    @Test
    fun appendLoggedFoodsAllowsSameNameWithDifferentKcalOrDate() {
        val existing = listOf(StoredMeal("chicken", 250, "2026-08-27"))
        val otherKcal = StoredMeal("chicken", 300, "2026-08-27")
        val otherDate = StoredMeal("chicken", 250, "2026-08-28")
        val updated = CalorieStore.appendLoggedFoods(existing, listOf(otherKcal, otherDate))
        assertEquals(3, updated.size)
    }

    @Test
    fun loggingSameScanTwiceAddsNothingSecondTime() {
        val scanMeals = CalorieStore.mealsFromScan(scanResult(listOf(chicken), 250), "2026-08-27")
        val once = CalorieStore.appendLoggedFoods(emptyList(), scanMeals)
        val twice = CalorieStore.appendLoggedFoods(once, scanMeals)
        assertEquals(once, twice)
    }

    @Test
    fun mealsFromScanLogsEditedValuesNotRawAiNumbers() {
        val original = scanResult(listOf(chicken), 250)
        val drafts = listOf(
            FoodEditDraft("chicken breast", "180 g", "300", "50", "2", "9")
        )
        val edited = FoodScanEdit.resultFromDrafts(original, drafts)!!
        val meals = CalorieStore.mealsFromScan(edited, "2026-08-27")
        assertEquals(1, meals.size)
        assertEquals("chicken breast", meals[0].name)
        assertEquals(300, meals[0].kcal)
        assertEquals(50.0, meals[0].proteinG, 0.0)
        assertEquals(2.0, meals[0].carbsG, 0.0)
        assertEquals(9.0, meals[0].fatG, 0.0)
    }

    @Test
    fun loggingSameEditedScanTwiceAddsNothingSecondTime() {
        val original = scanResult(listOf(chicken), 250)
        val edited = FoodScanEdit.resultFromDrafts(
            original,
            listOf(FoodEditDraft("chicken breast", "180 g", "300", "50", "2", "9"))
        )!!
        val meals = CalorieStore.mealsFromScan(edited, "2026-08-27")
        val once = CalorieStore.appendLoggedFoods(emptyList(), meals)
        val twice = CalorieStore.appendLoggedFoods(once, meals)
        assertEquals(1, twice.size)
        assertEquals(300, twice[0].kcal)
    }

    @Test
    fun editedThenRawAiScanAreNotTreatedAsDuplicates() {
        val original = scanResult(listOf(chicken), 250)
        val edited = FoodScanEdit.resultFromDrafts(
            original,
            listOf(FoodEditDraft.from(chicken).copy(calories = "300"))
        )!!
        val editedMeals = CalorieStore.mealsFromScan(edited, "2026-08-27")
        val rawMeals = CalorieStore.mealsFromScan(original, "2026-08-27")
        val updated = CalorieStore.appendLoggedFoods(editedMeals, rawMeals)
        assertEquals(2, updated.size)
        assertEquals(listOf(300, 250), updated.map { it.kcal })
    }
}
