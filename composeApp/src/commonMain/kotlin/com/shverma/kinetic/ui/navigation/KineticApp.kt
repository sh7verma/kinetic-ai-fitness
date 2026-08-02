package com.shverma.kinetic.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shverma.kinetic.ui.theme.AppTheme

enum class KineticAppRoute {
    Welcome,
    Onboarding,
    Landing,
}

/**
 * Shared route shell for the authenticated and onboarding flows.
 *
 * Platform adapters provide screen implementations and SDK-backed actions;
 * route ownership stays in common Compose code so Android and iOS do not need
 * separate Activity/navigation graphs.
 */
@Composable
fun KineticApp(
    welcomeContent: @Composable (
        snackbarHostState: SnackbarHostState,
        onNavigateToOnboarding: () -> Unit,
        onNavigateToLanding: () -> Unit,
    ) -> Unit,
    onboardingContent: @Composable (onFinish: () -> Unit) -> Unit,
    landingContent: @Composable (onLogout: () -> Unit) -> Unit,
    initialRoute: KineticAppRoute = KineticAppRoute.Welcome,
) {
    var route by remember { mutableStateOf(initialRoute) }
    val snackbarHostState = remember { SnackbarHostState() }

    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                when (route) {
                    KineticAppRoute.Welcome -> welcomeContent(
                        snackbarHostState,
                        { route = KineticAppRoute.Onboarding },
                        { route = KineticAppRoute.Landing },
                    )

                    KineticAppRoute.Onboarding -> onboardingContent {
                        route = KineticAppRoute.Landing
                    }

                    KineticAppRoute.Landing -> landingContent {
                        route = KineticAppRoute.Welcome
                    }
                }
            }
        }
    }
}
