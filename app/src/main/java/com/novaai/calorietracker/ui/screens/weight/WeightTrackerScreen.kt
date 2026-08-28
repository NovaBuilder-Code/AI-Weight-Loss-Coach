package com.novaai.calorietracker.ui.screens.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.WeightChartPoint
import com.novaai.calorietracker.data.WeightGoalStore
import com.novaai.calorietracker.data.WeightHistoryLogic
import com.novaai.calorietracker.data.WeightLog
import com.novaai.calorietracker.data.WeightStore
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*
import java.time.LocalDate

@Composable
fun WeightTrackerScreen(navController: NavController) {
    var showLogDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val history = remember {
        WeightStore.loadHistory(context).toMutableStateList()
    }
    var selectedAtMillis by remember { mutableStateOf<Long?>(null) }
    val current = WeightHistoryLogic.current(history.toList())?.kg
    val savedGoals = remember { WeightGoalStore.load(context) }
    var startKg by remember { mutableStateOf(savedGoals.startKg) }
    var goalKg by remember { mutableStateOf(savedGoals.goalKg) }
    val today = LocalDate.now()
    val chartPoints = WeightHistoryLogic.sevenDayPoints(history.toList(), today)
    val windowDates = WeightHistoryLogic.sevenDayWindow(today)
    val selectedLog = history.firstOrNull { it.recordedAtMillis == selectedAtMillis }
    val tapDetail = selectedLog?.let { WeightHistoryLogic.tapDetail(history.toList(), it) }

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
        item { WeightHeroSection(current, startKg, goalKg, chartPoints) }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            WeightChartCard(
                points = chartPoints,
                windowDates = windowDates,
                selectedAtMillis = selectedAtMillis,
                tapDetail = tapDetail,
                onSelect = { selectedAtMillis = it.recordedAtMillis }
            )
        }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            SectionHeader(
                title = stringResource(R.string.weight_section_history),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        items(history.size) { i ->
            val sorted = WeightHistoryLogic.sortedChronological(history.toList())
            val entry = sorted[sorted.size - 1 - i]
            WeightHistoryRow(entry, i == 0)
        }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            WeightGoalCard(
                current = current,
                startKg = startKg,
                goalKg = goalKg,
                onStartChange = { startKg = it },
                onGoalChange = { goalKg = it }
            )
        }
    }

    if (showLogDialog) {
        LogWeightDialog(
            title = stringResource(R.string.weight_dialog_title),
            onDismiss = { showLogDialog = false },
            onSave = { kg ->
                WeightStore.save(context, kg)
                val updated = WeightStore.loadHistory(context)
                history.clear()
                history.addAll(updated)
                selectedAtMillis = null
                showLogDialog = false
            }
        )
    }
}

@Composable
private fun WeightHeroSection(
    current: Float?,
    start: Float?,
    goal: Float?,
    chartPoints: List<WeightChartPoint>
) {
    val progress = WeightHistoryLogic.progressToGoal(current, start, goal)
    val weekDelta = if (chartPoints.size >= 2) {
        chartPoints.last().kg - chartPoints.first().kg
    } else {
        null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NovaGlowCard(modifier = Modifier.weight(1f)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.weight_current), style = MaterialTheme.typography.bodySmall, color = WhiteAlpha60)
                Text(
                    text = current?.let { "${it}kg" } ?: "—",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                if (weekDelta != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (weekDelta <= 0f) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${WeightHistoryLogic.formatDeltaKg(weekDelta)} this week",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenPrimary
                        )
                    }
                }
            }
        }

        NovaCard(modifier = Modifier.size(140.dp)) {
            RingProgress(
                progress = progress ?: 0f,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12f
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = WeightHistoryLogic.formatProgressPercent(progress),
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
private fun WeightChartCard(
    points: List<WeightChartPoint>,
    windowDates: List<LocalDate>,
    selectedAtMillis: Long?,
    tapDetail: com.novaai.calorietracker.data.WeightTapDetail?,
    onSelect: (WeightLog) -> Unit
) {
    NovaCard(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.weight_7day_trend), style = MaterialTheme.typography.titleLarge)
            WeightLineChart(
                points = points,
                selectedAtMillis = selectedAtMillis,
                onSelect = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                windowDates.forEach { date ->
                    Text(
                        text = WeightHistoryLogic.formatDateLabel(date),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        color = WhiteAlpha60,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (tapDetail != null) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = WeightHistoryLogic.formatDateLabel(tapDetail.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = White
                    )
                    Text(
                        text = WeightHistoryLogic.formatKg(tapDetail.kg),
                        style = MaterialTheme.typography.titleMedium,
                        color = GreenPrimary
                    )
                    WeightHistoryLogic.formatDeltaLine(tapDetail.deltaKg)?.let { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteAlpha60
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightLineChart(
    points: List<WeightChartPoint>,
    selectedAtMillis: Long?,
    onSelect: (WeightLog) -> Unit,
    modifier: Modifier = Modifier
) {
    val minW = (points.minOfOrNull { it.kg } ?: 0f) - 0.5f
    val maxW = (points.maxOfOrNull { it.kg } ?: 1f) + 0.5f
    val range = (maxW - minW).takeIf { it > 0f } ?: 1f
    val green = GreenPrimary
    val navy = NavyBorder
    val hitRadiusPx = with(LocalDensity.current) { 36.dp.toPx() }
    val slotCount = 7

    Canvas(
        modifier = modifier.pointerInput(points) {
            detectTapGestures { offset ->
                if (points.isEmpty()) return@detectTapGestures
                val w = size.width.toFloat()
                val h = size.height.toFloat()
                val step = if (slotCount <= 1) 0f else w / (slotCount - 1).toFloat()
                fun xOf(i: Int) = i * step
                fun yOf(v: Float) = h - ((v - minW) / range) * h
                val hit = points.minByOrNull { p ->
                    val dx = offset.x - xOf(p.dayIndex)
                    val dy = offset.y - yOf(p.kg)
                    dx * dx + dy * dy
                } ?: return@detectTapGestures
                val dx = offset.x - xOf(hit.dayIndex)
                val dy = offset.y - yOf(hit.kg)
                if (dx * dx + dy * dy <= hitRadiusPx * hitRadiusPx) {
                    onSelect(hit.log)
                }
            }
        }
    ) {
        val w = size.width
        val h = size.height
        val step = if (slotCount <= 1) 0f else w / (slotCount - 1)

        fun xOf(i: Int) = i * step
        fun yOf(v: Float) = h - ((v - minW) / range) * h

        repeat(4) { i ->
            val y = h * i / 3f
            drawLine(navy, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }

        if (points.isNotEmpty()) {
            val path = Path()
            val fill = Path()
            points.forEachIndexed { i, entry ->
                val x = xOf(entry.dayIndex)
                val y = yOf(entry.kg)
                if (i == 0) {
                    path.moveTo(x, y)
                    fill.moveTo(x, h)
                    fill.lineTo(x, y)
                } else {
                    val prev = points[i - 1]
                    val prevX = xOf(prev.dayIndex)
                    val prevY = yOf(prev.kg)
                    val dx = x - prevX
                    val cp1 = Offset(prevX + dx * 0.5f, prevY)
                    val cp2 = Offset(x - dx * 0.5f, y)
                    path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, x, y)
                    fill.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, x, y)
                }
            }
            fill.lineTo(xOf(points.last().dayIndex), h)
            fill.close()

            drawPath(fill, Brush.verticalGradient(listOf(green.copy(alpha = 0.25f), Color.Transparent)))
            if (points.size >= 2) {
                drawPath(path, green, style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }

            points.forEach { entry ->
                val selected = entry.log.recordedAtMillis == selectedAtMillis
                val center = Offset(xOf(entry.dayIndex), yOf(entry.kg))
                drawCircle(green, radius = if (selected) 8f else 5f, center = center)
                drawCircle(Color.White, radius = if (selected) 3.5f else 2.5f, center = center)
            }
        }
    }
}

@Composable
private fun WeightHistoryRow(entry: WeightLog, isLatest: Boolean) {
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
                Text(
                    text = WeightHistoryLogic.formatDateLabel(entry.localDate()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
                if (isLatest) {
                    Text(latestLabel, style = MaterialTheme.typography.labelSmall, color = GreenPrimary)
                }
            }
        }
        Text(
            text = WeightHistoryLogic.formatKg(entry.kg),
            style = MaterialTheme.typography.titleMedium,
            color = if (isLatest) GreenPrimary else White
        )
    }
}

@Composable
private fun WeightGoalCard(
    current: Float?,
    startKg: Float?,
    goalKg: Float?,
    onStartChange: (Float) -> Unit,
    onGoalChange: (Float) -> Unit
) {
    val context = LocalContext.current
    var showStartDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    val remaining = WeightHistoryLogic.remainingKg(current, startKg, goalKg)
    NovaCard(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.weight_goal_settings), style = MaterialTheme.typography.titleLarge)
            }
            StatChip(
                label = stringResource(R.string.weight_start),
                value = WeightHistoryLogic.formatOptionalKg(startKg),
                unit = if (startKg != null) stringResource(R.string.kg) else "",
                modifier = Modifier.fillMaxWidth().clickable { showStartDialog = true },
                accentColor = WhiteAlpha60
            )
            StatChip(
                label = stringResource(R.string.weight_goal),
                value = WeightHistoryLogic.formatOptionalKg(goalKg),
                unit = if (goalKg != null) stringResource(R.string.kg) else "",
                modifier = Modifier.fillMaxWidth().clickable { showGoalDialog = true },
                accentColor = GreenPrimary
            )
            StatChip(
                label = stringResource(R.string.weight_remaining),
                value = WeightHistoryLogic.formatOptionalKg(remaining),
                unit = if (remaining != null) stringResource(R.string.kg) else "",
                modifier = Modifier.fillMaxWidth(),
                accentColor = InfoBlue
            )
        }
    }

    if (showStartDialog) {
        LogWeightDialog(
            title = stringResource(R.string.weight_start_dialog_title),
            onDismiss = { showStartDialog = false },
            onSave = { kg ->
                WeightGoalStore.saveStart(context, kg)
                onStartChange(kg)
                showStartDialog = false
            }
        )
    }
    if (showGoalDialog) {
        LogWeightDialog(
            title = stringResource(R.string.weight_goal_dialog_title),
            onDismiss = { showGoalDialog = false },
            onSave = { kg ->
                WeightGoalStore.saveGoal(context, kg)
                onGoalChange(kg)
                showGoalDialog = false
            }
        )
    }
}

@Composable
private fun LogWeightDialog(title: String, onDismiss: () -> Unit, onSave: (Float) -> Unit) {
    var text by remember { mutableStateOf("") }
    val parsed = text.replace(',', '.').toFloatOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text(title, color = White) },
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
            TextButton(
                onClick = { parsed?.let { onSave(it) } },
                enabled = parsed != null && parsed in 20f..300f
            ) {
                Text(stringResource(R.string.save), color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = WhiteAlpha60) }
        }
    )
}
