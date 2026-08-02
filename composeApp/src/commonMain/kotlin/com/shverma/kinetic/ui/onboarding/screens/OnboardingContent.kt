package com.shverma.kinetic.ui.onboarding.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.shverma.kinetic.ui.onboarding.OnboardingActions
import com.shverma.kinetic.ui.onboarding.OnboardingStep

@Composable
fun OnboardingContent(
    actions: OnboardingActions,
    onFinish: () -> Unit,
) {
    val uiState by actions.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.currentStep) {
            OnboardingStep.BIOMETRICS -> OnboardingBiometricsStep(
                viewModel = actions,
                onContinue = actions::nextStep,
            )
            OnboardingStep.GOALS -> OnboardingGoalsStep(
                viewModel = actions,
                onContinue = actions::nextStep,
            )
            OnboardingStep.RESULTS -> OnboardingResultsStep(
                viewModel = actions,
                onFinish = onFinish,
            )
        }
    }
}
