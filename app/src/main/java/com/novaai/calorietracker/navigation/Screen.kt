package com.novaai.calorietracker.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.ui.graphics.vector.ImageVector
import com.novaai.calorietracker.R

sealed class Screen(val route: String) {
    // — First launch —
    data object Onboarding   : Screen("onboarding")
    data object ProfileSetup : Screen("profile_setup")

    // — Bottom-nav destinations —
    data object Home         : Screen("home")
    data object Chat         : Screen("chat")
    data object Weight       : Screen("weight")
    data object Walking      : Screen("walking")

    // — Additional full screens —
    data object AICoach      : Screen("ai_coach")
    data object WeightGoals  : Screen("weight_goals")
    data object FoodScan     : Screen("food_scan")
    data object Calories     : Screen("calories")
    data object Meals        : Screen("meals")
    data object Streaks      : Screen("streaks")
    data object Profile      : Screen("profile")
    data object Subscription : Screen("subscription")

    // — Profile sub-screens —
    data object PersonalInfo : Screen("personal_info")
    data object EditProfile  : Screen("edit_profile")
    data object Goals        : Screen("goals")
    data object Notifications: Screen("notifications")
    data object Privacy      : Screen("privacy")
    data object HelpSupport  : Screen("help_support")
    data object About        : Screen("about")
    data object Settings     : Screen("settings")

    // — Home stat screens —
    data object Water        : Screen("water")
    data object Sleep        : Screen("sleep")
}

data class BottomNavItem(
    val screen: Screen,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home,    R.string.nav_home,    Icons.Default.Home),
    BottomNavItem(Screen.Chat,    R.string.nav_chat,    Icons.Default.Chat),
    BottomNavItem(Screen.Weight,  R.string.nav_weight,  Icons.Default.MonitorWeight),
    BottomNavItem(Screen.Walking, R.string.nav_steps,   Icons.Default.DirectionsWalk)
)
