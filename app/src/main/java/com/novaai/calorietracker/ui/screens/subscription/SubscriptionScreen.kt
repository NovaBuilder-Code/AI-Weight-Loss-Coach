package com.novaai.calorietracker.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*
import kotlinx.coroutines.launch

private data class PlanOption(
    val id: String,
    val labelRes: Int,
    val price: String,
    val periodRes: Int,
    val badgeRes: Int? = null,
    val pricePerMonthRes: Int
)

@Composable
fun SubscriptionScreen(navController: NavController) {
    val plans = listOf(
        PlanOption("monthly",  R.string.subscription_plan_monthly,  "$9.99",  R.string.subscription_period_month,  null,                              R.string.subscription_price_monthly_pm),
        PlanOption("annual",   R.string.subscription_plan_annual,   "$59.99", R.string.subscription_period_year,   R.string.subscription_badge_save50, R.string.subscription_price_annual_pm),
        PlanOption("lifetime", R.string.subscription_plan_lifetime, "$149.99",R.string.subscription_period_onetime,R.string.subscription_badge_best_value, R.string.subscription_price_lifetime_pm),
    )

    val premiumFeatures = listOf(
        Triple(Icons.Default.CameraAlt,          R.string.subscription_feature_scan_title,     R.string.subscription_feature_scan_sub),
        Triple(Icons.Default.Psychology,          R.string.subscription_feature_coach_title,    R.string.subscription_feature_coach_sub),
        Triple(Icons.Default.BarChart,            R.string.subscription_feature_analytics_title,R.string.subscription_feature_analytics_sub),
        Triple(Icons.Default.RestaurantMenu,      R.string.subscription_feature_meals_title,    R.string.subscription_feature_meals_sub),
        Triple(Icons.Default.LocalFireDepartment, R.string.subscription_feature_streaks_title,  R.string.subscription_feature_streaks_sub),
        Triple(Icons.Default.CloudSync,           R.string.subscription_feature_sync_title,     R.string.subscription_feature_sync_sub),
    )

    var selectedPlan by remember { mutableStateOf("annual") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val trialMsg = stringResource(R.string.subscription_snackbar_trial)
    val termsMsg = stringResource(R.string.subscription_snackbar_terms)
    val privacyMsg = stringResource(R.string.subscription_snackbar_privacy_policy)
    val restoreMsg = stringResource(R.string.subscription_snackbar_restore)

    fun showMessage(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    Scaffold(
        containerColor = NavyDeep,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            NovaTopBar(title = stringResource(R.string.subscription_title), onBack = { navController.popBackStack() })
        }
        item { PremiumHero() }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                plans.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        isSelected = selectedPlan == plan.id,
                        onSelect = { selectedPlan = plan.id }
                    )
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            NovaPrimaryButton(
                text = stringResource(R.string.subscription_cta),
                onClick = { showMessage(trialMsg) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item {
            Text(
                text = stringResource(R.string.subscription_cancel_note),
                style = MaterialTheme.typography.bodySmall,
                color = WhiteAlpha60,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
        item { Spacer(Modifier.height(28.dp)) }
        item {
            SectionHeader(
                title = stringResource(R.string.subscription_section_features),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        items(premiumFeatures.size) { i ->
            val (icon, titleRes, subRes) = premiumFeatures[i]
            FeatureRow(icon, stringResource(titleRes), stringResource(subRes))
        }
        item { Spacer(Modifier.height(20.dp)) }
        item { TermsRow(
            termsLabel = stringResource(R.string.subscription_terms),
            privacyLabel = stringResource(R.string.subscription_privacy),
            restoreLabel = stringResource(R.string.subscription_restore),
            onTerms = { showMessage(termsMsg) },
            onPrivacy = { showMessage(privacyMsg) },
            onRestore = { showMessage(restoreMsg) }) }
    }
    } // end Scaffold
}

@Composable
private fun PremiumHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(GreenTintCard, NavyElevated, GreenPrimary.copy(alpha = 0.06f))
                )
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NovaAvatar(size = 80.dp)
            Text(stringResource(R.string.subscription_hero_headline), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(
                text = stringResource(R.string.subscription_hero_body),
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteAlpha60,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PlanCard(plan: PlanOption, isSelected: Boolean, onSelect: () -> Unit) {
    val borderColor = if (isSelected) GreenPrimary else NavyBorder
    val bg = if (isSelected) GreenPrimary.copy(alpha = 0.08f) else NavyElevated

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = GreenPrimary,
                    unselectedColor = WhiteAlpha30
                )
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(plan.labelRes), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(plan.pricePerMonthRes), style = MaterialTheme.typography.bodySmall, color = WhiteAlpha60)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = plan.price,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isSelected) GreenPrimary else White
                )
                Text(stringResource(plan.periodRes), style = MaterialTheme.typography.labelSmall, color = WhiteAlpha60)
            }
            if (plan.badgeRes != null) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GreenPrimary.copy(alpha = 0.20f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(stringResource(plan.badgeRes), color = GreenPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GreenPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = WhiteAlpha60)
        }
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun TermsRow(
    termsLabel: String,
    privacyLabel: String,
    restoreLabel: String,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit,
    onRestore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onTerms) { Text(termsLabel, color = WhiteAlpha60, fontSize = 12.sp) }
        Text("·", color = WhiteAlpha60, modifier = Modifier.align(Alignment.CenterVertically))
        TextButton(onClick = onPrivacy) { Text(privacyLabel, color = WhiteAlpha60, fontSize = 12.sp) }
        Text("·", color = WhiteAlpha60, modifier = Modifier.align(Alignment.CenterVertically))
        TextButton(onClick = onRestore) { Text(restoreLabel, color = WhiteAlpha60, fontSize = 12.sp) }
    }
}
