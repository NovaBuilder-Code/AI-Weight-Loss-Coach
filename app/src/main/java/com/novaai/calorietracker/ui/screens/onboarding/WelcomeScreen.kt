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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.UserProfileStore
import com.novaai.calorietracker.navigation.Screen
import com.novaai.calorietracker.ui.components.NovaPrimaryButton
import com.novaai.calorietracker.ui.theme.*

/** Fixed hero height (rather than the full uncropped aspect ratio) so the screen fits on one page;
 *  paired with the top pivot this only trims the lower shoulder/torso, never the face. */
private val HERO_IMAGE_HEIGHT = 500.dp

/**
 * Horizontal centre of Nova's portrait inside the hero asset (0..1 of its
 * width). The asset places the portrait left-of-centre; the rendered image is
 * zoomed by [HERO_ZOOM] and shifted so the portrait itself sits at screen
 * centre, and pulled back up vertically so the head keeps its original height.
 * Everything is a fraction of the box width, so it stays centred on any screen
 * size.
 */
private const val HERO_PORTRAIT_CENTER = 0.355f

/**
 * Hero zoom factor. 14B.2: reduced from the 14B.1 value (0.5 / centre ≈ 1.41)
 * to 1.15, making the portrait ~18 % smaller and showing more of the original
 * portrait (head, hair and shoulders) instead of a tight crop.
 */
private const val HERO_ZOOM = 1.15f

/** Vertical position of the top of Nova's head inside the source, as a fraction
 *  of the source WIDTH (the hero is layout-scaled by width on phones). */
private const val HERO_HEAD_TOP_SOURCE = 120f / 471f

private data class OnboardingFeature(
    val icon: ImageVector,
    val labelRes: Int,
    val route: String? = null
)

@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(HERO_IMAGE_HEIGHT)
                .clipToBounds()
        ) {
            // Uniform zoom + shift: the portrait lands exactly at screen
            // centre, the right side (incl. the green glow) overflows and is
            // clipped, and the head is pulled back up to its original height.
            // The smaller portrait no longer reaches the box's left edge, so a
            // soft navy fade covers that margin. Everything is a fraction of
            // the width, so it stays centred on any screen size.
            val heroGapDp = maxWidth * (0.5f - HERO_PORTRAIT_CENTER * HERO_ZOOM)
            val headTopDp = maxWidth * HERO_HEAD_TOP_SOURCE
            Image(
                painter = painterResource(R.drawable.nova_hero),
                contentDescription = stringResource(R.string.onboarding_hero_cd),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = HERO_ZOOM
                        scaleY = HERO_ZOOM
                        transformOrigin = TransformOrigin(0f, 0f)
                        translationX = heroGapDp.toPx()
                        translationY = (headTopDp * (1f - HERO_ZOOM)).toPx()
                    }
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(heroGapDp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(NavyDeep, androidx.compose.ui.graphics.Color.Transparent)
                        )
                    )
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
                    // First launch: collect the required profile answers before Home.
                    val profile = UserProfileStore.load(context)
                    val setupNeeded =
                        profile.name.isNullOrBlank() || profile.age == null || profile.sex == null ||
                            profile.units == null || profile.heightCm == null ||
                            profile.currentWeightKg == null || profile.goalWeightKg == null ||
                            profile.mainGoal == null || profile.activityLevel == null ||
                            profile.dailyStepGoal == null
                    if (setupNeeded) {
                        navController.navigate(Screen.ProfileSetup.route)
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
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
