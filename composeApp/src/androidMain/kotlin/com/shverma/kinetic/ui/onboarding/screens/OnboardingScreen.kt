package com.shverma.kinetic.ui.onboarding.screens

import androidx.compose.runtime.Composable
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    onFinish: () -> Unit,
) {
    OnboardingContent(actions = viewModel, onFinish = onFinish)
}
