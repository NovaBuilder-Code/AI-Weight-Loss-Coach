package com.novaai.calorietracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.CalorieStore
import com.novaai.calorietracker.data.StepsStore
import com.novaai.calorietracker.data.WaterStore
import com.novaai.calorietracker.data.WeightStore
import com.novaai.calorietracker.navigation.Screen
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

@Composable
fun HomeScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { HomeHeader(navController) }
        item { Spacer(Modifier.height(20.dp)) }
        item { CalorieSummaryCard(navController) }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            SectionHeader(
                title = stringResource(R.string.home_section_todays_stats),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        item { TodayStatsRow(navController) }
        item { Spacer(Modifier.height(20.dp)) }
        item { WeightGoalsCard(navController) }
        item { Spacer(Modifier.height(12.dp)) }
        item { DailyStreaksCard(navController) }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            SectionHeader(
                title = stringResource(R.string.home_section_quick_actions),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        item { QuickActionsGrid(navController) }
        item { Spacer(Modifier.height(24.dp)) }
        item { NovaAIInsightCard(navController) }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            SectionHeader(
                title = stringResource(R.string.home_section_macro_breakdown),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        item { MacroBreakdownCard() }
    }
}

@Composable
private fun HomeHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(R.string.home_greeting),
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteAlpha60
            )
            Text(
                text = stringResource(R.string.home_user_name),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CircleIconButton(
                icon = Icons.Default.LocalFireDepartment,
                contentDescription = stringResource(R.string.home_streaks_cd),
                onClick = { navController.navigate(Screen.Streaks.route) },
                tint = WarningAmber
            )
            CircleIconButton(
                icon = Icons.Default.Person,
                contentDescription = stringResource(R.string.home_profile_cd),
                onClick = { navController.navigate(Screen.Profile.route) },
                tint = GreenPrimary
            )
        }
    }
}

@Composable
private fun CalorieSummaryCard(navController: NavController) {
    val context = LocalContext.current
    val consumed = remember { CalorieStore.loadTodayMeals(context).sumOf { it.kcal } }
    val goal = 2000
    val progress = (consumed.toFloat() / goal).coerceIn(0f, 1f)

    NovaGlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { navController.navigate(Screen.Calories.route) }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.home_calories_today),
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteAlpha60
            )
            Spacer(Modifier.height(16.dp))
            Box(contentAlignment = Alignment.Center) {
                RingProgress(
                    progress = progress,
                    modifier = Modifier.size(160.dp),
                    strokeWidth = 14f,
                    progressColor = GreenPrimary,
                    trackColor = NavyBorder
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$consumed",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text(
                            text = stringResource(R.string.home_goal_format, goal),
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteAlpha60
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalorieMiniStat(stringResource(R.string.home_remaining), "${goal - consumed}", stringResource(R.string.kcal), GreenPrimary)
                CalorieMiniStat(stringResource(R.string.home_burned), "340", stringResource(R.string.kcal), WarningAmber)
                CalorieMiniStat(stringResource(R.string.home_net), "${consumed - 340}", stringResource(R.string.kcal), InfoBlue)
            }
        }
    }
}

@Composable
private fun CalorieMiniStat(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = unit, style = MaterialTheme.typography.labelSmall, color = WhiteAlpha60)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = WhiteAlpha60)
    }
}

private data class TodayStat(
    val label: String,
    val value: String,
    val unit: String,
    val icon: ImageVector,
    val color: Color,
    val route: String? = null
)

@Composable
private fun TodayStatsRow(navController: NavController) {
    val context = LocalContext.current
    val stepsText = remember { "%,d".format(StepsStore.loadToday(context)) }
    val waterLiters = remember { WaterStore.loadToday(context).intakeMl / 1000f }
    val weightText = remember {
        WeightStore.load(context)?.kg?.let { kg ->
            if (kg % 1f == 0f) "%.0f".format(kg) else kg.toString()
        } ?: "74.2"
    }
    val todayStats = listOf(
        TodayStat(stringResource(R.string.home_stat_steps),  stepsText, stringResource(R.string.steps_unit), Icons.Default.DirectionsWalk, GreenPrimary, Screen.Walking.route),
        TodayStat(stringResource(R.string.home_stat_weight), weightText, stringResource(R.string.kg),      Icons.Default.MonitorWeight,  InfoBlue,     Screen.Weight.route),
        TodayStat(stringResource(R.string.home_stat_water),  "%.1f".format(waterLiters), stringResource(R.string.liters), Icons.Default.WaterDrop, Color(0xFF40CFFF), Screen.Water.route),
        TodayStat(stringResource(R.string.home_stat_sleep),  "7h 20m","",                                  Icons.Default.NightsStay,     Color(0xFF9B8FFF), Screen.Sleep.route)
    )
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        todayStats.chunked(2).forEach { pair ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(IntrinsicSize.Max)
            ) {
                pair.forEach { stat ->
                    StatTile(
                        stat = stat,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { stat.route?.let { navController.navigate(it) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatTile(stat: TodayStat, modifier: Modifier = Modifier, onClick: () -> Unit) {
    NovaCard(
        modifier = modifier.clickable(onClick = onClick),
        cornerRadius = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(stat.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = stat.label,
                    tint = stat.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = stat.value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Text(text = stat.unit, style = MaterialTheme.typography.labelSmall, color = WhiteAlpha60)
            Text(text = stat.label, style = MaterialTheme.typography.labelSmall, color = WhiteAlpha60)
        }
    }
}

@Composable
private fun WeightGoalsCard(navController: NavController) {
    NovaCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { navController.navigate(Screen.WeightGoals.route) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(InfoBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = InfoBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.home_weight_goals_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = White
                )
                Text(
                    text = stringResource(R.string.home_weight_goals_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteAlpha60
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = WhiteAlpha30,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun DailyStreaksCard(navController: NavController) {
    NovaCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { navController.navigate(Screen.Streaks.route) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(WarningAmber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.home_streaks_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = White
                )
                Text(
                    text = stringResource(R.string.home_streaks_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteAlpha60
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = WhiteAlpha30,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(navController: NavController) {
    val actions = listOf(
        Triple(stringResource(R.string.home_action_scan_food),   Icons.Default.CameraAlt,      Screen.FoodScan.route),
        Triple(stringResource(R.string.home_action_add_meal),    Icons.Default.RestaurantMenu,  Screen.Calories.route),
        Triple(stringResource(R.string.home_action_meal_ideas),  Icons.Default.Lightbulb,       Screen.Meals.route),
        Triple(stringResource(R.string.home_action_premium),     Icons.Default.Star,            Screen.Subscription.route)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.chunked(2).forEach { pair ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { (label, icon, route) ->
                    QuickActionButton(label, icon) { navController.navigate(route) }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NavyElevated)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = GreenPrimary, modifier = Modifier.size(22.dp))
        Text(text = label, style = MaterialTheme.typography.titleMedium, color = White)
    }
}

@Composable
private fun NovaAIInsightCard(navController: NavController) {
    NovaCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { navController.navigate(Screen.Chat.route) },
        gradient = Brush.linearGradient(listOf(GreenTintCard, NavyElevated))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NovaAvatar(size = 52.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.home_insight_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = GreenPrimary
                )
                Text(
                    text = stringResource(R.string.home_insight_message),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = WhiteAlpha30,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MacroBreakdownCard() {
    NovaCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MacroBar(stringResource(R.string.home_macro_protein), "92g",  "150g", 0.61f, GreenPrimary)
            MacroBar(stringResource(R.string.home_macro_carbs),   "180g", "250g", 0.72f, InfoBlue)
            MacroBar(stringResource(R.string.home_macro_fat),     "44g",  "65g",  0.68f, WarningAmber)
            MacroBar(stringResource(R.string.home_macro_fiber),   "18g",  "30g",  0.60f, Color(0xFF9B8FFF))
        }
    }
}

@Composable
private fun MacroBar(name: String, current: String, goal: String, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium)
            Text(text = "$current / $goal", style = MaterialTheme.typography.bodySmall, color = WhiteAlpha60)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = NavyBorder
        )
    }
}
