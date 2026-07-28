package com.shverma.kinetic.ui.onboarding.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel
import com.shverma.kinetic.ui.onboarding.components.OnboardingContinueButton
import com.shverma.kinetic.ui.theme.KineticShape
import com.shverma.kinetic.ui.theme.KineticSpacing
import com.shverma.kinetic.ui.theme.KineticTheme

@Composable
fun OnboardingResultsStep(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    LaunchedEffect(Unit) {
        if (uiState.targetCaloriesData == null) {
            viewModel.saveUserProfileData()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(KineticSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(KineticSpacing.xxl))

        if (uiState.isLoading || uiState.targetCaloriesData == null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = colors.primary)
                    Spacer(Modifier.height(KineticSpacing.lg))
                    Text(
                        text = "Calculating your targets...",
                        style = typography.bodyMd,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        } else {
            val targets = uiState.targetCaloriesData!!

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.confidenceHighContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = colors.confidenceHighText)
            }
            Spacer(Modifier.height(KineticSpacing.lg))
            Text(text = "Your daily targets", style = typography.titleLg, color = colors.onSurface)
            Spacer(Modifier.height(KineticSpacing.sm))
            Text(
                text = "Based on the Mifflin–St Jeor formula × your activity factor, adjusted for your goal.",
                style = typography.bodyMd,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(KineticSpacing.xl))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(KineticShape.card))
                    .background(colors.surfaceContainer)
                    .padding(KineticSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${targets.targetCalories.toInt()}",
                        style = typography.displayMd,
                        color = colors.onSurface,
                    )
                    Text(
                        text = " kcal / day",
                        style = typography.bodyMd,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                Spacer(Modifier.height(KineticSpacing.lg))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    MacroStat(label = "Protein", value = "${targets.proteinG.toInt()}g")
                    MacroStat(label = "Carbs", value = "${targets.carbsG.toInt()}g")
                    MacroStat(label = "Fat", value = "${targets.fatsG.toInt()}g")
                }
            }

            Spacer(Modifier.weight(1f))
            OnboardingContinueButton(text = "Start logging", onClick = onFinish)
        }
    }
}

@Composable
private fun MacroStat(label: String, value: String) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = typography.titleLg, color = colors.onSurface)
        Text(text = label, style = typography.bodySm, color = colors.onSurfaceVariant)
    }
}
