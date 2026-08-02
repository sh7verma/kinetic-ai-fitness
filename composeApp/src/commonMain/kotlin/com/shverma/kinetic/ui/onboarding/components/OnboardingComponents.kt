package com.shverma.kinetic.ui.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shverma.kinetic.ui.onboarding.OnboardingStep
import com.shverma.kinetic.ui.theme.KineticShape
import com.shverma.kinetic.ui.theme.KineticSpacing
import com.shverma.kinetic.ui.theme.KineticTheme

@Composable
fun OnboardingHeader(
    step: OnboardingStep,
    modifier: Modifier = Modifier,
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = KineticSpacing.lg, vertical = KineticSpacing.md),
    ) {
        Text(
            text = "STEP ${step.stepNumber} OF ${OnboardingStep.totalSteps}",
            style = typography.labelMd,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(KineticSpacing.xs))
        Text(text = step.title, style = typography.titleLg, color = colors.onSurface)
        Spacer(Modifier.height(KineticSpacing.lg))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KineticSpacing.xs),
        ) {
            repeat(OnboardingStep.totalSteps) { index ->
                val isFilled = index < step.stepNumber
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (isFilled) colors.primary else colors.surfaceContainerHigh),
                )
            }
        }
        Spacer(Modifier.height(KineticSpacing.xl))
    }
}

@Composable
fun OnboardingContinueButton(
    text: String = "Continue",
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KineticShape.button))
            .background(if (enabled) colors.primary else colors.surfaceContainerHigh)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = typography.titleSm,
            color = if (enabled) colors.onPrimary else colors.onSurfaceVariant,
        )
    }
}
