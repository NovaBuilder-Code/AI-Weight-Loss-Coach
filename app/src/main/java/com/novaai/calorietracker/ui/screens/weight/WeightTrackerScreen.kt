package com.novaai.calorietracker.ui.screens.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

private data class WeightEntry(val day: String, val kg: Float)

private val weightHistory = listOf(
    WeightEntry("Jun 19", 75.8f),
    WeightEntry("Jun 20", 75.5f),
    WeightEntry("Jun 21", 75.2f),
    WeightEntry("Jun 22", 75.6f),
    WeightEntry("Jun 23", 74.9f),
    WeightEntry("Jun 24", 74.6f),
    WeightEntry("Jun 25", 74.2f),
)

@Composable
fun WeightTrackerScreen(navController: NavController) {
    var showLogDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            NovaTopBar(title = stringResource(R.string.weight_tracker_title), onBack = { navController.popBackStack() }) {
                CircleIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = stringResource(R.string.weight_log_cd),
                    onClick = { showLogDialog = true }
                )
            }
        }
        item { WeightHeroSection() }
        item { Spacer(Modifier.height(20.dp)) }
        item { WeightChartCard() }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            SectionHeader(
                title = stringResource(R.string.weight_section_history),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        items(weightHistory.size) { i ->
            val entry = weightHistory[weightHistory.size - 1 - i]
            WeightHistoryRow(entry, i == 0)
        }
        item { Spacer(Modifier.height(20.dp)) }
        item { WeightGoalCard() }
    }

    if (showLogDialog) {
        LogWeightDialog(onDismiss = { showLogDialog = false })
    }
}

@Composable
private fun WeightHeroSection() {
    val current = 74.2f
    val start   = 80.0f
    val goal    = 70.0f
    val progress = ((start - current) / (start - goal)).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Big weight display
        NovaGlowCard(modifier = Modifier.weight(1f)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.weight_current), style = MaterialTheme.typography.bodySmall, color = WhiteAlpha60)
                Text(
                    text = "${current}kg",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.weight_this_week),
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenPrimary
                    )
                }
            }
        }

        // Progress ring
        NovaCard(modifier = Modifier.size(140.dp)) {
            RingProgress(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12f
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                    Text(stringResource(R.string.weight_to_goal), style = MaterialTheme.typography.labelSmall, color = WhiteAlpha60)
                }
            }
        }
    }
}

@Composable
private fun WeightChartCard() {
    NovaCard(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.weight_7day_trend), style = MaterialTheme.typography.titleLarge)
            WeightLineChart(
                data = weightHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
            // X-axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weightHistory.forEach { entry ->
                    Text(
                        text = entry.day.takeLast(2),
                        style = MaterialTheme.typography.labelSmall,
                        color = WhiteAlpha60
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightLineChart(data: List<WeightEntry>, modifier: Modifier = Modifier) {
    val minW = data.minOf { it.kg } - 0.5f
    val maxW = data.maxOf { it.kg } + 0.5f
    val green = GreenPrimary
    val navy  = NavyBorder

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val step = w / (data.size - 1)

        fun xOf(i: Int) = i * step
        fun yOf(v: Float) = h - ((v - minW) / (maxW - minW)) * h

        // Grid lines
        repeat(4) { i ->
            val y = h * i / 3f
            drawLine(navy, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }

        val path = Path()
        val fill = Path()
        data.forEachIndexed { i, entry ->
            val x = xOf(i)
            val y = yOf(entry.kg)
            if (i == 0) {
                path.moveTo(x, y)
                fill.moveTo(x, h)
                fill.lineTo(x, y)
            } else {
                val prevX = xOf(i - 1)
                val prevY = yOf(data[i - 1].kg)
                val cp1 = Offset(prevX + step * 0.5f, prevY)
                val cp2 = Offset(x - step * 0.5f, y)
                path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, x, y)
                fill.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, x, y)
            }
        }
        fill.lineTo(xOf(data.size - 1), h)
        fill.close()

        drawPath(fill, Brush.verticalGradient(listOf(green.copy(alpha = 0.25f), Color.Transparent)))
        drawPath(path, green, style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        data.forEachIndexed { i, entry ->
            drawCircle(green, radius = 5f, center = Offset(xOf(i), yOf(entry.kg)))
            drawCircle(Color.White, radius = 2.5f, center = Offset(xOf(i), yOf(entry.kg)))
        }
    }
}

@Composable
private fun WeightHistoryRow(entry: WeightEntry, isLatest: Boolean) {
    val latestLabel = stringResource(R.string.weight_latest)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isLatest) GreenPrimary.copy(alpha = 0.08f) else NavyElevated)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                imageVector = Icons.Default.MonitorWeight,
                contentDescription = null,
                tint = if (isLatest) GreenPrimary else WhiteAlpha60,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(text = entry.day, style = MaterialTheme.typography.bodyMedium, color = White)
                if (isLatest) {
                    Text(latestLabel, style = MaterialTheme.typography.labelSmall, color = GreenPrimary)
                }
            }
        }
        Text(
            text = "${entry.kg} kg",
            style = MaterialTheme.typography.titleMedium,
            color = if (isLatest) GreenPrimary else White
        )
    }
}

@Composable
private fun WeightGoalCard() {
    NovaCard(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.weight_goal_settings), style = MaterialTheme.typography.titleLarge)
            }
            StatChip(label = stringResource(R.string.weight_start),     value = "80.0", unit = stringResource(R.string.kg), modifier = Modifier.fillMaxWidth(), accentColor = WhiteAlpha60)
            StatChip(label = stringResource(R.string.weight_goal),      value = "70.0", unit = stringResource(R.string.kg), modifier = Modifier.fillMaxWidth(), accentColor = GreenPrimary)
            StatChip(label = stringResource(R.string.weight_remaining), value = "4.2",  unit = stringResource(R.string.kg), modifier = Modifier.fillMaxWidth(), accentColor = InfoBlue)
        }
    }
}

@Composable
private fun LogWeightDialog(onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text(stringResource(R.string.weight_dialog_title), color = White) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.weight_dialog_label), color = WhiteAlpha60) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = NavyBorder,
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    cursorColor = GreenPrimary,
                    focusedContainerColor = NavyElevated,
                    unfocusedContainerColor = NavyElevated
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.save), color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = WhiteAlpha60) }
        }
    )
}
