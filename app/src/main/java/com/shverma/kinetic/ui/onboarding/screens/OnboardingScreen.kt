package com.shverma.kinetic.ui.onboarding.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.kinetic.ui.onboarding.OnboardingStep
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.currentStep) {
            OnboardingStep.BIOMETRICS -> {
                OnboardingBiometricsStep(
                    viewModel = viewModel,
                    onContinue = { viewModel.nextStep() }
                )
            }

            OnboardingStep.GOALS -> {
                OnboardingGoalsStep(
                    viewModel = viewModel,
                    onContinue = { viewModel.nextStep() }
                )
            }

            OnboardingStep.RESULTS -> {
                OnboardingResultsStep(
                    viewModel = viewModel,
                    onFinish = onFinish
                )
            }
        }
    }
}
