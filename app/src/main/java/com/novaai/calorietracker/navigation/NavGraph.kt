package com.novaai.calorietracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.novaai.calorietracker.ui.screens.aicoach.AICoachScreen
import com.novaai.calorietracker.ui.screens.calories.CalorieTrackerScreen
import com.novaai.calorietracker.ui.screens.chat.ChatScreen
import com.novaai.calorietracker.ui.screens.foodscan.FoodScanScreen
import com.novaai.calorietracker.ui.screens.home.HomeScreen
import com.novaai.calorietracker.ui.screens.meals.MealSuggestionsScreen
import com.novaai.calorietracker.ui.screens.onboarding.ProfileSetupScreen
import com.novaai.calorietracker.ui.screens.onboarding.WelcomeScreen
import com.novaai.calorietracker.ui.screens.profile.AboutScreen
import com.novaai.calorietracker.ui.screens.profile.GoalsScreen
import com.novaai.calorietracker.ui.screens.profile.HelpSupportScreen
import com.novaai.calorietracker.ui.screens.profile.NotificationsScreen
import com.novaai.calorietracker.ui.screens.profile.PersonalInfoScreen
import com.novaai.calorietracker.ui.screens.profile.PrivacyScreen
import com.novaai.calorietracker.ui.screens.profile.ProfileScreen
import com.novaai.calorietracker.ui.screens.settings.SettingsScreen
import com.novaai.calorietracker.ui.screens.sleep.SleepTrackerScreen
import com.novaai.calorietracker.ui.screens.streaks.DailyStreaksScreen
import com.novaai.calorietracker.ui.screens.subscription.SubscriptionScreen
import com.novaai.calorietracker.ui.screens.walking.WalkingTrackerScreen
import com.novaai.calorietracker.ui.screens.water.WaterTrackerScreen
import com.novaai.calorietracker.ui.screens.weight.WeightTrackerScreen
import com.novaai.calorietracker.ui.screens.weightgoals.WeightGoalsScreen

@Composable
fun NovaNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            WelcomeScreen(navController = navController)
        }
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(navController = navController)
        }

        // Bottom nav
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Chat.route) {
            ChatScreen(navController = navController)
        }
        composable(Screen.Weight.route) {
            WeightTrackerScreen(navController = navController)
        }
        composable(Screen.Walking.route) {
            WalkingTrackerScreen(navController = navController)
        }

        // Feature screens
        composable(Screen.AICoach.route) {
            AICoachScreen(navController = navController)
        }
        composable(Screen.WeightGoals.route) {
            WeightGoalsScreen(navController = navController)
        }
        composable(Screen.FoodScan.route) {
            FoodScanScreen(navController = navController)
        }
        composable(Screen.Calories.route) {
            CalorieTrackerScreen(navController = navController)
        }
        composable(Screen.Meals.route) {
            MealSuggestionsScreen(navController = navController)
        }
        composable(Screen.Streaks.route) {
            DailyStreaksScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Subscription.route) {
            SubscriptionScreen(navController = navController)
        }

        // Profile sub-screens
        composable(Screen.PersonalInfo.route) {
            PersonalInfoScreen(navController = navController)
        }
        composable(Screen.Goals.route) {
            GoalsScreen(navController = navController)
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
        composable(Screen.Privacy.route) {
            PrivacyScreen(navController = navController)
        }
        composable(Screen.HelpSupport.route) {
            HelpSupportScreen(navController = navController)
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // Home stat screens
        composable(Screen.Water.route) {
            WaterTrackerScreen(navController = navController)
        }
        composable(Screen.Sleep.route) {
            SleepTrackerScreen(navController = navController)
        }
    }
}
