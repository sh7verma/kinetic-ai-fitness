package com.shverma.kinetic.ui.welcome

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WelcomeScreen(
    snackBarHostState: SnackbarHostState? = null,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLanding: () -> Unit,
    viewModel: WelcomeViewModel = koinViewModel(),
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    WelcomeContent(
        authState = authState,
        snackBarHostState = snackBarHostState,
        onSignInWithGoogle = {
            viewModel.signInWithGoogle()
        },
        onClearError = viewModel::clearError,
        onNavigateToOnboarding = onNavigateToOnboarding,
        onNavigateToLanding = onNavigateToLanding,
    )
}
