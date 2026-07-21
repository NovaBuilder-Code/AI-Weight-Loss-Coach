package com.novaai.calorietracker.ui.screens.walking

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.StepsStore
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*
import androidx.annotation.StringRes
import java.time.LocalDate
import kotlin.math.roundToInt

private data class DaySteps(@StringRes val labelRes: Int, val steps: Int, val goal: Int = 10_000)

// No weekly history storage yet: every day shows an empty bar.
// Monday first, matching DayOfWeek.value (Monday == 1).
private val weeklySteps = listOf(
    DaySteps(R.string.streaks_day_mon, 0),
    DaySteps(R.string.streaks_day_tue, 0),
    DaySteps(R.string.streaks_day_wed, 0),
    DaySteps(R.string.streaks_day_thu, 0),
    DaySteps(R.string.streaks_day_fri, 0),
    DaySteps(R.string.streaks_day_sat, 0),
    DaySteps(R.string.streaks_day_sun, 0),
)

@Composable
fun WalkingTrackerScreen(navController: NavController) {
    val context = LocalContext.current
    var isTracking by remember { mutableStateOf(false) }
    var todaySteps by remember { mutableIntStateOf(StepsStore.loadToday(context)) }

    LaunchedEffect(todaySteps) { StepsStore.save(context, todaySteps) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            NovaTopBar(title = stringResource(R.string.walking_tracker_title), onBack = { navController.popBackStack() })
        }
        item { Spacer(Modifier.height(8.dp)) }
        item { StepHeroCard(todaySteps, isTracking) { isTracking = !isTracking } }
        item { Spacer(Modifier.height(20.dp)) }
        item { WalkingStatsRow(todaySteps) }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            SectionHeader(
                title = stringResource(R.string.walking_section_this_week),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        item { WeeklyBarChart() }
        item { Spacer(Modifier.height(20.dp)) }
        item { AchievementsCard() }
    }
}

@Composable
private fun StepHeroCard(todaySteps: Int, isTracking: Boolean, onToggle: () -> Unit) {
    val goal = 10_000
    val progress = todaySteps.toFloat() / goal

    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    NovaCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        gradient = Brush.linearGradient(
            listOf(GreenTintCard, NavyElevated)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.walking_todays_steps), style = MaterialTheme.typography.bodyMedium, color = WhiteAlpha60)

            RingProgress(
                progress = progress,
                modifier = Modifier
                    .size(180.dp)
                    .scale(if (isTracking) pulseScale else 1f),
                strokeWidth = 16f,
                progressColor = GreenPrimary,
                trackColor = NavyBorder
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "%,d".format(todaySteps),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = "/ %,d".format(goal),
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteAlpha60
                    )
                }
            }

            // Toggle tracking button
            Button(
                onClick = onToggle,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) ErrorRed else GreenPrimary,
                    contentColor = NavyDeep
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = if (isTracking) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isTracking) stringResource(R.string.walking_pause_tracking) else stringResource(R.string.walking_start_tracking),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun WalkingStatsRow(todaySteps: Int) {
    // MVP estimates derived from the saved step count, never stored separately.
    val distanceKm = todaySteps * 0.00075
    val calories = (todaySteps * 0.04).roundToInt()
    val minutes = todaySteps / 100
    val durationText = if (minutes < 60) "${minutes}m" else "${minutes / 60}h ${minutes % 60}m"
    val stats = listOf(
        Triple(stringResource(R.string.walking_stat_distance), "%.1f".format(distanceKm), stringResource(R.string.km)),
        Triple(stringResource(R.string.walking_stat_calories), calories.toString(), stringResource(R.string.kcal)),
        Triple(stringResource(R.string.walking_stat_duration), durationText, "")
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { (label, value, unit) ->
            StatChip(
                label = label,
                value = value,
                unit = unit,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WeeklyBarChart() {
    val goalReachedLabel = stringResource(R.string.walking_legend_goal_reached)
    val belowGoalLabel = stringResource(R.string.walking_legend_below_goal)
    val todayLabel = stringResource(R.string.walking_legend_today)

    NovaCard(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.walking_weekly_overview), style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val todayIndex = LocalDate.now().dayOfWeek.value - 1
                weeklySteps.forEachIndexed { index, day ->
                    val frac = (day.steps.toFloat() / day.goal).coerceIn(0f, 1f)
                    val isToday = index == todayIndex
                    DayBar(
                        label = stringResource(day.labelRes),
                        fraction = frac,
                        isToday = isToday,
                        reachedGoal = day.steps > 0 && day.steps >= day.goal
                    )
                }
            }
            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(GreenPrimary, goalReachedLabel)
                LegendDot(NavyBorder, belowGoalLabel)
                LegendDot(InfoBlue, todayLabel)
            }
        }
    }
}

@Composable
private fun DayBar(label: String, fraction: Float, isToday: Boolean, reachedGoal: Boolean) {
    val barColor = when {
        isToday     -> InfoBlue
        reachedGoal -> GreenPrimary
        else        -> NavyBorder
    }
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(600, easing = EaseOut),
        label = "barFraction"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(NavyElevated)
            )
            // Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedFraction)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.verticalGradient(listOf(barColor, barColor.copy(alpha = 0.6f)))
                    )
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) InfoBlue else WhiteAlpha60
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = WhiteAlpha60)
    }
}

@Composable
private fun AchievementsCard() {
    // No achievement tracking yet: show an empty state instead of sample rows.
    NovaCard(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.walking_achievements_title), style = MaterialTheme.typography.titleLarge)
            }
            Column {
                Text(stringResource(R.string.walking_ach_empty_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.walking_ach_empty_sub), style = MaterialTheme.typography.bodySmall, color = WhiteAlpha60)
            }
        }
    }
}
