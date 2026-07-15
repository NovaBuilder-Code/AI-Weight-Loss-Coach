package com.novaai.calorietracker.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.navigation.Screen
import com.novaai.calorietracker.ui.components.NovaPrimaryButton
import com.novaai.calorietracker.ui.theme.*

/** Fixed hero height (rather than the full uncropped aspect ratio) so the screen fits on one page;
 *  paired with Alignment.TopCenter this only trims the lower shoulder/torso, never the face. */
private val HERO_IMAGE_HEIGHT = 470.dp

private data class OnboardingFeature(
    val icon: ImageVector,
    val labelRes: Int,
    val route: String? = null
)

@Composable
fun WelcomeScreen(navController: NavController) {
    val features = listOf(
        OnboardingFeature(Icons.Default.SmartToy, R.string.onboarding_feature_ai_coach, Screen.AICoach.route),
        OnboardingFeature(Icons.Default.LocalFireDepartment, R.string.onboarding_feature_calorie_tracking, Screen.Calories.route),
        OnboardingFeature(Icons.Default.CameraAlt, R.string.onboarding_feature_photo_scan, Screen.FoodScan.route),
        OnboardingFeature(Icons.Default.DirectionsWalk, R.string.onboarding_feature_walking_goals, Screen.Walking.route),
        OnboardingFeature(Icons.Default.MonitorWeight, R.string.onboarding_feature_weight_goals, Screen.WeightGoals.route),
        OnboardingFeature(Icons.Default.RestaurantMenu, R.string.onboarding_feature_meal_suggestions, Screen.Meals.route),
        OnboardingFeature(Icons.Default.EmojiEvents, R.string.onboarding_feature_daily_streaks, Screen.Streaks.route),
        OnboardingFeature(Icons.Default.NightsStay, R.string.onboarding_feature_sleep_tracker, Screen.Sleep.route)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HERO_IMAGE_HEIGHT)
        ) {
            Image(
                painter = painterResource(R.drawable.nova_hero),
                contentDescription = stringResource(R.string.onboarding_hero_cd),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.75f to androidx.compose.ui.graphics.Color.Transparent,
                            1f to NavyDeep
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.onboarding_brand),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = White
            )
            Text(
                text = stringResource(R.string.onboarding_brand_sub),
                style = MaterialTheme.typography.labelLarge,
                color = GreenPrimary,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.onboarding_headline),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            FeatureChipsGrid(features, navController)

            Spacer(Modifier.height(20.dp))

            NovaPrimaryButton(
                text = stringResource(R.string.onboarding_cta),
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun FeatureChipsGrid(features: List<OnboardingFeature>, navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        features.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { feature ->
                    FeatureChip(
                        icon = feature.icon,
                        label = stringResource(feature.labelRes),
                        modifier = Modifier.weight(1f),
                        onClick = feature.route?.let { route -> { navController.navigate(route) } }
                    )
                }
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FeatureChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NavyElevated)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = White)
    }
}
