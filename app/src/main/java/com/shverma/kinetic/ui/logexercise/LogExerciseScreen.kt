package com.shverma.kinetic.ui.logexercise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.kinetic.ui.components.KineticExerciseHeader
import com.shverma.kinetic.ui.components.KineticMetricCard
import com.shverma.kinetic.ui.components.KineticPrimaryButton
import com.shverma.kinetic.ui.components.KineticTopAppBar
import com.shverma.kinetic.ui.components.KineticUpNextCard
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.ui.theme.LexendFamily
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily

@Composable
fun LogExerciseScreen(
    onBackClick: () -> Unit = {},
    viewModel: LogExerciseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Scaffold(
        containerColor = Color(0xFF0E0E0E), // Obsidian Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text(
                        text = "←",
                        color = Color.White,
                        fontSize = 28.sp
                    )
                }
            }

            ExerciseHeader(
                tag = state.exerciseTag,
                title = state.exerciseName
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(
                    label = "SETS",
                    value = state.sets.toString(),
                    onIncrement = { viewModel.onEvent(LogExerciseEvent.IncrementSets) },
                    onDecrement = { viewModel.onEvent(LogExerciseEvent.DecrementSets) }
                )
                MetricCard(
                    label = "REPS",
                    value = state.reps.toString(),
                    onIncrement = { viewModel.onEvent(LogExerciseEvent.IncrementReps) },
                    onDecrement = { viewModel.onEvent(LogExerciseEvent.DecrementReps) }
                )
                MetricCard(
                    label = "WEIGHT (KG)",
                    value = if (state.weight % 1 == 0f) state.weight.toInt()
                        .toString() else state.weight.toString(),
                    onIncrement = { viewModel.onEvent(LogExerciseEvent.IncrementWeight) },
                    onDecrement = { viewModel.onEvent(LogExerciseEvent.DecrementWeight) }
                )
            }

            Text(
                text = state.lastPerformance,
                style = typography.labelSm.copy(
                    fontFamily = SpaceGroteskFamily,
                    color = Color(0xFFA3A3A3).copy(alpha = 0.6f)
                )
            )

            KineticPrimaryButton(
                text = "LOG SET",
                onClick = { viewModel.onEvent(LogExerciseEvent.LogSet) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )

            UpNextCard(
                totalSets = state.totalSets,
                targetReps = state.targetReps,
                targetWeight = state.targetWeight
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ExerciseHeader(tag: String, title: String) {
    val typography = KineticTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            color = Color(0xFFCCFF00),
            shape = CircleShape
        ) {
            Text(
                text = tag,
                style = typography.labelSm.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = Color.Black
            )
        }
        Text(
            text = title,
            style = typography.displayMd.copy(
                fontSize = 38.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Black,
                lineHeight = 44.sp
            ),
            color = Color.White
        )
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val typography = KineticTheme.typography
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313)) // Charcoal
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = typography.labelSm.copy(
                        fontFamily = SpaceGroteskFamily,
                        color = Color(0xFFA3A3A3)
                    )
                )
                Text(
                    text = value,
                    style = typography.titleLg.copy(
                        fontFamily = LexendFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IncrementButton(icon = Icons.Default.Remove, onClick = onDecrement)
                IncrementButton(icon = Icons.Default.Add, onClick = onIncrement)
            }
        }
    }
}

@Composable
fun IncrementButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF1A1A1A)) // Control Background
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFCCFF00), // Volt Neon Lime
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun UpNextCard(
    totalSets: Int,
    targetReps: Int,
    targetWeight: Float
) {
    val typography = KineticTheme.typography
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "NEXT SESSION TARGET »",
                style = typography.labelSm.copy(
                    fontFamily = SpaceGroteskFamily,
                    color = Color(0xFFA3A3A3)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SET $totalSets",
                        style = typography.titleMd.copy(
                            fontFamily = LexendFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "TARGET: $targetReps REPS · ${if (targetWeight % 1 == 0f) targetWeight.toInt() else targetWeight} KG",
                        style = typography.bodyMd.copy(
                            fontFamily = SpaceGroteskFamily,
                            color = Color(0xFFA3A3A3)
                        )
                    )
                }

                // Optional icon
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}


@Preview
@Composable
fun LogExerciseScreenPreview() {
    KineticTheme {
        LogExerciseScreen()
    }
}
