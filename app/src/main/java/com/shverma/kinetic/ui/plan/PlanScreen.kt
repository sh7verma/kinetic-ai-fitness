package com.shverma.kinetic.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shverma.kinetic.ui.components.KineticPrimaryButton
import com.shverma.kinetic.ui.components.KineticTopAppBar
import com.shverma.kinetic.ui.theme.KineticTheme

@Composable
fun PlanScreen(
    onLogComplete: () -> Unit = {},
    viewModel: PlanViewModel = hiltViewModel()
) {
    val colors = KineticTheme.colors
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = colors.background,
        topBar = { KineticTopAppBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                WeeklyStreakCard(
                    streakDays = state.streakDays,
                    streakProgress = state.streakProgress,
                    totalStreakDays = state.totalStreakDays
                )
            }
            item { DailyFocusCard(title = state.dailyFocusTitle) }
            item { WorkoutHeader() }
            itemsIndexed(state.exercises) { index, exercise ->
                ExerciseCard(index + 1, exercise, onLogComplete)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) } // Space for bottom nav
        }
    }
}


@Composable
fun DailyFocusCard(title: String) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawVerticalNeonLine(colors.primaryContainer)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colors.surfaceContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🧠", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = typography.titleMd,
                    color = colors.onSurface
                )
                Text(
                    text = buildAnnotatedString {
                        append("Focus on the ")
                        withStyle(SpanStyle(color = colors.primaryContainer, fontWeight = FontWeight.Bold)) {
                            append("eccentric phase")
                        }
                        append(" to maximize motor unit recruitment during today's power session.")
                    },
                    style = typography.bodyMd,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WorkoutHeader() {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = "POWER",
                style = typography.displayMd.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "HYPERTROPHY",
                style = typography.displayLg.copy(
                    fontStyle = FontStyle.Italic,
                    fontSize = 48.sp,
                    lineHeight = 48.sp
                ),
                color = colors.primaryContainer
            )
        }
    }
}

@Composable
fun ExerciseCard(number: Int, exercise: Exercise, onLogComplete: () -> Unit = {}) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // TOP ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = number.toString().padStart(2, '0'),
                        style = typography.titleMd,
                        color = colors.primaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = exercise.name.uppercase(),
                        style = typography.titleMd.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Surface(
                    color = colors.surfaceContainer,
                    shape = RoundedCornerShape(fullPillRadius),
                    modifier = Modifier.border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(fullPillRadius))
                ) {
                    Text(
                        text = exercise.tag,
                        style = typography.labelSm,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MIDDLE SECTION (Metrics Grid)
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricColumn("SETS", exercise.sets, Modifier.weight(1f))
                MetricColumn("REPS", exercise.reps, Modifier.weight(1f))
                MetricColumn(
                    "WEIGHT",
                    exercise.weight,
                    Modifier.weight(1f),
                    valueColor = if (exercise.isHighIntensity) colors.error else colors.primaryContainer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTTOM SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KineticPrimaryButton(
                    text = "LOG COMPLETE",
                    onClick = onLogComplete,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(colors.surfaceContainer, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MetricColumn(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Color.White) {
    val typography = KineticTheme.typography
    val colors = KineticTheme.colors

    Column(modifier = modifier) {
        Text(
            text = label,
            style = typography.labelSm,
            color = colors.onSurfaceVariant
        )
        Text(
            text = value,
            style = typography.titleLg.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

@Composable
fun WeeklyStreakCard(
    streakDays: Int,
    streakProgress: Int,
    totalStreakDays: Int
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$streakDays DAYS",
                        style = typography.displayMd.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streakProgress/$totalStreakDays COMPLETED",
                        style = typography.labelMd,
                        color = colors.primaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(totalStreakDays) { index ->
                    val isCompleted = index < streakProgress
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(fullPillRadius))
                            .background(
                                if (isCompleted) colors.primaryContainer else colors.surfaceContainer
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.drawVerticalNeonLine(color: Color): Modifier = this.then(
    Modifier.padding(start = 0.dp).width(IntrinsicSize.Min) // Placeholder for actual implementation if needed
).drawBehindNeonLine(color)

@Composable
private fun Modifier.drawBehindNeonLine(color: Color): Modifier {
    return this.background(
        color = color,
        shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
    ).padding(start = 4.dp).background(KineticTheme.colors.surfaceContainerLow)
}

val fullPillRadius = 100.dp
