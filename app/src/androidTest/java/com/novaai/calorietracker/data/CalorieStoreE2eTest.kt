package com.novaai.calorietracker.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * End-to-end round trip on a real device: scan result -> Log Food ->
 * today's calorie log. The real "calorie_tracker" prefs are backed up
 * before each test and restored afterwards so user data is never touched.
 */
@RunWith(AndroidJUnit4::class)
class CalorieStoreE2eTest {

    private lateinit var context: Context
    private var backupMeals: String? = null

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        backupMeals = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MEALS, null)
    }

    @After
    fun tearDown() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (backupMeals == null) remove(KEY_MEALS) else putString(KEY_MEALS, backupMeals)
        }.commit()
    }

    private val scanResult = FoodScanResult(
        foods = listOf(
            FoodItem("grilled chicken", "150 g", 250, 40.0, 0.0, 8.0),
            FoodItem("brown rice", "1 bowl", 215, 5.0, 45.0, 2.0)
        ),
        totalCalories = 465,
        confidence = "medium",
        disclaimer = "AI estimate — portions and calories may vary."
    )

    @Test
    fun loggedScanAppearsInTodaysLogWithCaloriesAndMacros() {
        assertEquals(2, CalorieStore.logScanResult(context, scanResult))

        val today = CalorieStore.loadTodayMeals(context)
        val chicken = today.first { it.name == "grilled chicken" }
        assertEquals(250, chicken.kcal)
        assertEquals(40.0, chicken.proteinG, 0.0)
        assertEquals(0.0, chicken.carbsG, 0.0)
        assertEquals(8.0, chicken.fatG, 0.0)
        assertEquals(LocalDate.now().toString(), chicken.date)

        val rice = today.first { it.name == "brown rice" }
        assertEquals(215, rice.kcal)
        assertEquals(5.0, rice.proteinG, 0.0)
        assertEquals(45.0, rice.carbsG, 0.0)
    }

    @Test
    fun todayTotalCaloriesIncreaseAfterLogging() {
        val before = CalorieStore.loadTodayMeals(context).sumOf { it.kcal }
        CalorieStore.logScanResult(context, scanResult)
        val after = CalorieStore.loadTodayMeals(context).sumOf { it.kcal }
        assertEquals(before + 465, after)
    }

    @Test
    fun loggingSameScanTwiceDoesNotDuplicate() {
        CalorieStore.logScanResult(context, scanResult)
        assertEquals(0, CalorieStore.logScanResult(context, scanResult))

        val meals = CalorieStore.loadTodayMeals(context)
        assertEquals(1, meals.count { it.name == "grilled chicken" })
        assertEquals(1, meals.count { it.name == "brown rice" })
    }

    @Test
    fun loggedMealSurvivesStorageReload() {
        CalorieStore.logScanResult(context, scanResult)
        val freshContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val reloaded = CalorieStore.loadTodayMeals(freshContext)
        assertTrue(reloaded.any { it.name == "grilled chicken" && it.kcal == 250 && it.proteinG == 40.0 })
    }

    private companion object {
        const val PREFS_NAME = "calorie_tracker"
        const val KEY_MEALS = "meals"
    }
}