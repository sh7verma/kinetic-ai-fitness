package com.shverma.kinetic.ui.onboarding.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shverma.kinetic.ui.onboarding.OnboardingActions
import com.shverma.kinetic.ui.onboarding.OnboardingStep
import com.shverma.kinetic.ui.onboarding.components.OnboardingContinueButton
import com.shverma.kinetic.ui.onboarding.components.OnboardingHeader
import com.shverma.kinetic.ui.theme.KineticShape
import com.shverma.kinetic.ui.theme.KineticSpacing
import com.shverma.kinetic.ui.theme.KineticTheme

val GOALS = listOf("Lose", "Maintain", "Gain")

/** Keys match MacrosCalculator.calculate's activityMultiplier lookup exactly. */
data class ActivityOption(val key: String, val label: String)

val ACTIVITY_OPTIONS = listOf(
    ActivityOption("SEDENTARY", "Sedentary — little to no exercise"),
    ActivityOption("LIGHT", "Lightly active — 1–3 days/week"),
    ActivityOption("MODERATE", "Moderately active — 3–5 days/week"),
    ActivityOption("ACTIVE", "Active — 6–7 days/week"),
    ActivityOption("VERY ACTIVE", "Very active — intense daily training"),
)

@Composable
fun OnboardingGoalsStep(
    viewModel: OnboardingActions,
    onContinue: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = KineticSpacing.xxl),
    ) {
        OnboardingHeader(step = OnboardingStep.GOALS)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KineticSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(KineticSpacing.xl),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm)) {
                Text(text = "Goal", style = typography.labelLg, color = colors.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(KineticSpacing.sm),
                ) {
                    GOALS.forEach { goal ->
                        SegmentOption(
                            text = goal,
                            isSelected = uiState.workoutGoal == goal,
                            onClick = { viewModel.updateWorkoutGoal(goal) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm)) {
                Text(text = "Activity level", style = typography.labelLg, color = colors.onSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm)) {
                    ACTIVITY_OPTIONS.forEach { option ->
                        ActivityOptionRow(
                            label = option.label,
                            isSelected = uiState.selectedActivityLevel == option.key,
                            onClick = { viewModel.updateActivityLevel(option.key) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(KineticSpacing.sm))
            OnboardingContinueButton(onClick = onContinue)
        }
    }
}

@Composable
fun SegmentOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KineticTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(KineticShape.pill))
            .background(if (isSelected) colors.primary else colors.surfaceContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = KineticTheme.typography.titleSm,
            color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
        )
    }
}

@Composable
fun ActivityOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KineticShape.card))
            .background(if (isSelected) colors.primary.copy(alpha = 0.08f) else colors.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(KineticSpacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = KineticTheme.typography.bodyMd,
                color = if (isSelected) colors.primary else colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (isSelected) {
                RadioDot()
            }
        }
    }
}

@Composable
private fun RadioDot() {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(colors.primary),
    )
}
