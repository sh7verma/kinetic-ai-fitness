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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shverma.kinetic.ui.onboarding.OnboardingStep
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel
import com.shverma.kinetic.ui.onboarding.components.OnboardingContinueButton
import com.shverma.kinetic.ui.onboarding.components.OnboardingHeader
import com.shverma.kinetic.ui.theme.KineticShape
import com.shverma.kinetic.ui.theme.KineticSpacing
import com.shverma.kinetic.ui.theme.KineticTheme

@Composable
fun OnboardingBiometricsStep(
    viewModel: OnboardingViewModel,
    onContinue: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KineticTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = KineticSpacing.xxl),
    ) {
        OnboardingHeader(step = OnboardingStep.BIOMETRICS)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KineticSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(KineticSpacing.lg),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(KineticSpacing.md)) {
                NumberField(
                    label = "Age",
                    value = uiState.age,
                    onValueChange = viewModel::updateAge,
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    label = "Weight (kg)",
                    value = uiState.weight,
                    onValueChange = { viewModel.updateWeight(it, uiState.weightUnit) },
                    modifier = Modifier.weight(1f),
                )
            }

            NumberField(
                label = "Height (cm)",
                value = uiState.height,
                onValueChange = { viewModel.updateHeight(it, uiState.heightUnit) },
            )

            Column(verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm)) {
                Text(text = "Sex", style = KineticTheme.typography.labelLg, color = colors.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(KineticSpacing.sm),
                ) {
                    SexOption(
                        text = "Male",
                        isSelected = uiState.sex == "MALE",
                        onClick = { viewModel.updateSex("MALE") },
                        modifier = Modifier.weight(1f),
                    )
                    SexOption(
                        text = "Female",
                        isSelected = uiState.sex == "FEMALE",
                        onClick = { viewModel.updateSex("FEMALE") },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(KineticSpacing.lg))
            OnboardingContinueButton(onClick = onContinue)
        }
    }
}

@Composable
fun NumberField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KineticTheme.colors
    OutlinedTextField(
        value = if (value == 0.0) "" else value.toInt().toString(),
        onValueChange = { text -> text.toDoubleOrNull()?.let(onValueChange) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(KineticShape.pill),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceContainer,
            unfocusedContainerColor = colors.surfaceContainer,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outlineVariant.copy(alpha = 0.09f),
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.onSurfaceVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun SexOption(
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
