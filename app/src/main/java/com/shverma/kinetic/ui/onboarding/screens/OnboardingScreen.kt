package com.shverma.kinetic.ui.onboarding.screens

import com.shverma.kinetic.ui.onboarding.OnboardingStep
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel
import com.shverma.kinetic.ui.onboarding.components.OnboardingTopBar
import com.shverma.kinetic.ui.onboarding.components.OnboardingStepProgressBar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.kinetic.ui.theme.*

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column {
                OnboardingTopBar(
                    currentStep = uiState.currentStep.stepNumber,
                    totalSteps = uiState.totalSteps,
                    onBack = { if (!uiState.isLoading) viewModel.previousStep() },
                    onSkip = { if (!uiState.isLoading) viewModel.skipOnboarding(onFinish) }
                )
                OnboardingStepProgressBar(
                    currentStep = uiState.currentStep.stepNumber,
                    uiState.totalSteps
                )

            }
        },
        containerColor = MealBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState.currentStep) {
                OnboardingStep.BIOMETRICS -> {
                    OnboardingBiometricsStep(
                        viewModel = viewModel,
                        onBack = { viewModel.previousStep() },
                        onContinue = { viewModel.nextStep() }
                    )
                }

                OnboardingStep.WORKOUT_SETUP -> {
                    OnboardingWorkoutSetupStep(
                        viewModel = viewModel,
                        onBack = { viewModel.previousStep() },
                        onContinue = { viewModel.nextStep() }
                    )
                }

                OnboardingStep.MEAL_SETUP -> {
                    OnboardingMealSetupStep(
                        viewModel = viewModel,
                        currentStep = uiState.currentStep,
                        onBack = { viewModel.previousStep() },
                        onContinue = { viewModel.nextStep() }
                    )
                }

                OnboardingStep.ACTIVITY_SETUP -> {
                    OnboardingActivityStep(
                        viewModel = viewModel,
                        onBack = { viewModel.previousStep() },
                        onContinue = { viewModel.nextStep() }
                    )
                }

                OnboardingStep.FLAVOR_PROTOCOL -> {
                    OnboardingFlavorProtocolStep(
                        viewModel = viewModel,
                        onContinue = onFinish
                    )
                }
            }
        }
    }
}
