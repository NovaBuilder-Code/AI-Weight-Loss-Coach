package com.novaai.calorietracker.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserProfileStoreTest {

    private lateinit var context: Context

    private val sampleProfile = UserProfile(
        name = "Alex",
        age = 34,
        sex = Sex.FEMALE,
        heightCm = 172.5f,
        currentWeightKg = 74.2f,
        goalWeightKg = 68f,
        mainGoal = MainGoal.LOSE_WEIGHT,
        activityLevel = ActivityLevel.MODERATELY_ACTIVE,
        dailyStepGoal = 10_000,
        units = MeasurementUnits.METRIC
    )

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        UserProfileStore.clear(context)
    }

    @After
    fun tearDown() {
        UserProfileStore.clear(context)
    }

    @Test
    fun savedProfileIsReadBackWithAllFields() {
        UserProfileStore.save(context, sampleProfile)
        assertEquals(sampleProfile, UserProfileStore.load(context))
    }

    @Test
    fun emptyStoreLoadsAsAllUnanswered() {
        assertEquals(UserProfile(), UserProfileStore.load(context))
    }

    @Test
    fun partialProfileKeepsUnansweredFieldsNull() {
        UserProfileStore.save(context, UserProfile(name = "Alex", mainGoal = MainGoal.GAIN_WEIGHT))
        val loaded = UserProfileStore.load(context)
        assertEquals("Alex", loaded.name)
        assertEquals(MainGoal.GAIN_WEIGHT, loaded.mainGoal)
        assertNull(loaded.age)
        assertNull(loaded.sex)
        assertNull(loaded.heightCm)
        assertNull(loaded.dailyStepGoal)
        assertNull(loaded.units)
    }

    @Test
    fun resavingWithNullFieldClearsThePreviousAnswer() {
        UserProfileStore.save(context, sampleProfile)
        UserProfileStore.save(context, sampleProfile.copy(dailyStepGoal = null, name = null))
        val loaded = UserProfileStore.load(context)
        assertNull(loaded.dailyStepGoal)
        assertNull(loaded.name)
        assertEquals(sampleProfile.heightCm, loaded.heightCm)
    }

    @Test
    fun profileSurvivesStorageReload() {
        UserProfileStore.save(context, sampleProfile)
        // Reload through a separate context; save() uses apply(), which the
        // framework guarantees to flush to disk, so the profile outlives the
        // process like every other SharedPreferences store in the app.
        val freshContext = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext
        assertEquals(sampleProfile, UserProfileStore.load(freshContext))
    }

    @Test
    fun editedFieldsUpdateWhileOthersStay() {
        UserProfileStore.save(context, sampleProfile)
        // Exactly what the Edit Profile screen does: merge changed fields
        // into the stored profile and save it back.
        UserProfileStore.save(
            context,
            UserProfileStore.load(context).copy(
                name = "Sam",
                age = 41,
                currentWeightKg = 71.5f,
                activityLevel = ActivityLevel.VERY_ACTIVE,
                dailyStepGoal = 12_000
            )
        )
        val edited = UserProfileStore.load(context)
        assertEquals("Sam", edited.name)
        assertEquals(41, edited.age)
        assertEquals(71.5f, edited.currentWeightKg)
        assertEquals(ActivityLevel.VERY_ACTIVE, edited.activityLevel)
        assertEquals(12_000, edited.dailyStepGoal)
        // Unchanged fields must survive the edit.
        assertEquals(sampleProfile.sex, edited.sex)
        assertEquals(sampleProfile.heightCm, edited.heightCm)
        assertEquals(sampleProfile.goalWeightKg, edited.goalWeightKg)
        assertEquals(sampleProfile.mainGoal, edited.mainGoal)
        assertEquals(sampleProfile.units, edited.units)
    }

    @Test
    fun clearRemovesEverySavedField() {
        UserProfileStore.save(context, sampleProfile)
        UserProfileStore.clear(context)
        assertEquals(UserProfile(), UserProfileStore.load(context))
    }
}
