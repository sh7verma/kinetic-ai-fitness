package com.shverma.kinetic.ui.onboarding.screens

import androidx.compose.runtime.Composable
import com.shverma.kinetic.ui.onboarding.OnboardingStep
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel
import com.shverma.kinetic.ui.onboarding.components.OnboardingStepHeader
import com.shverma.kinetic.ui.onboarding.components.OnboardingBottomNavigation
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.shverma.kinetic.ui.theme.LexendFamily
import com.shverma.kinetic.ui.theme.LocalKineticTypography
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily
import com.shverma.kinetic.ui.theme.KineticTertiary
import com.shverma.kinetic.ui.theme.KineticSecondary
import com.shverma.kinetic.ui.theme.MealVolt

@Composable
fun OnboardingWorkoutSetupStep(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep = uiState.currentStep
    
    val selectedGoal = uiState.workoutGoal
    val commitmentDays = uiState.commitmentDays
    val selectedEquipment = uiState.equipment

    val typography = LocalKineticTypography.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OnboardingStepHeader(
                title = currentStep.title,
                subtitle = currentStep.subtitle,
                description = currentStep.description
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                MissionDefinitionSection(
                    selectedGoal = selectedGoal,
                    onGoalSelected = { viewModel.updateWorkoutGoal(it) }
                )

                CommitmentScaleSection(
                    days = commitmentDays,
                    onDaysChange = { viewModel.updateCommitmentDays(it.toDouble()) }
                )

                GearDeploymentSection(
                    selectedEquipment = selectedEquipment,
                    onEquipmentSelected = { viewModel.updateEquipment(it) }
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Bottom Navigation Buttons
        OnboardingBottomNavigation(
            onContinue = onContinue,
            onBack = onBack,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun MissionDefinitionSection(selectedGoal: String, onGoalSelected: (String) -> Unit) {
    val goals = remember {
        listOf(
            GoalItem("HYPERTROPHY", "Build Lean Muscle", Icons.Default.FitnessCenter, MealVolt),
            GoalItem("FAT LOSS", "Aggressive Shred", Icons.Default.LocalFireDepartment, KineticSecondary),
            GoalItem("PERFORMANCE", "Athletic Power", Icons.Default.Bolt, KineticTertiary)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        goals.forEach { goal ->
            val isSelected = goal.mainTitle == selectedGoal
            GoalCard(
                goal = goal,
                isSelected = isSelected,
                onClick = { onGoalSelected(goal.mainTitle) }
            )
        }
    }
}

data class GoalItem(val subLabel: String, val mainTitle: String, val icon: ImageVector, val color: Color)

@Composable
fun GoalCard(goal: GoalItem, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131313))
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 0.dp, // Left accent only, handled by padding/background trick or simple Row
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent border for selected state
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(80.dp) // Approximate height
                        .background(goal.color)
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = goal.subLabel,
                        style = LocalKineticTypography.current.labelSm.copy(
                            fontFamily = SpaceGroteskFamily,
                            color = goal.color,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = goal.mainTitle,
                        style = LocalKineticTypography.current.titleMd.copy(
                            fontFamily = LexendFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Icon(
                    imageVector = goal.icon,
                    contentDescription = null,
                    tint = goal.color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun CommitmentScaleSection(days: Double, onDaysChange: (Float) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = days.toInt().toString(),
            style = LocalKineticTypography.current.displayMd.copy(
                fontFamily = LexendFamily,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = Color.White,
                fontSize = 48.sp
            )
        )
        Text(
            text = "DAYS / WEEK",
            style = LocalKineticTypography.current.labelMd.copy(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        KineticDiamondSlider(
            value = days.toFloat(),
            onValueChange = onDaysChange,
            valueRange = 1f..7f
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KineticDiamondSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        colors = SliderDefaults.colors(
            thumbColor = KineticTertiary,
            activeTrackColor = KineticTertiary,
            inactiveTrackColor = Color(0xFF131313),
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.White.copy(alpha = 0.2f)
        ),
        thumb = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape, spotColor = KineticTertiary)
                    .clip(RoundedCornerShape(4.dp)) // Diamond shape roughly via rotation or specific shape
                    .background(KineticTertiary)
                    .graphicsLayer(rotationZ = 45f)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                colors = SliderDefaults.colors(
                    activeTrackColor = KineticTertiary,
                    inactiveTrackColor = Color(0xFF131313)
                ),
                modifier = Modifier.height(4.dp)
            )
        }
    )
}

@Composable
fun GearDeploymentSection(selectedEquipment: String, onEquipmentSelected: (String) -> Unit) {
    val equipments = listOf(
        EquipmentItem("FULL GYM", Icons.Default.Business),
        EquipmentItem("HOME LAB", Icons.Default.Home),
        EquipmentItem("MINIMAL", Icons.Default.FitnessCenter)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        equipments.forEach { equipment ->
            val isSelected = equipment.label == selectedEquipment
            EquipmentCard(
                equipment = equipment,
                isSelected = isSelected,
                modifier = Modifier.weight(1f),
                onClick = { onEquipmentSelected(equipment.label) }
            )
        }
    }
}

data class EquipmentItem(val label: String, val icon: ImageVector)

@Composable
fun EquipmentCard(equipment: EquipmentItem, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131313))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) KineticTertiary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = equipment.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = equipment.label,
                style = LocalKineticTypography.current.labelSm.copy(
                    fontFamily = SpaceGroteskFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun InitializeProtocolButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCCFF00),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "INITIALIZE PROTOCOL",
                    style = LocalKineticTypography.current.titleMd.copy(
                        fontFamily = LexendFamily,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
