package com.shverma.kinetic

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.shverma.kinetic.data.auth.AndroidActivityProvider
import com.shverma.kinetic.ui.aichat.AIChatScreen
import com.shverma.kinetic.ui.fuel.FuelScreen
import com.shverma.kinetic.ui.navigation.KineticApp
import com.shverma.kinetic.ui.navigation.KineticLandingContent
import com.shverma.kinetic.ui.onboarding.screens.OnboardingScreen
import com.shverma.kinetic.ui.profile.ProfileScreen
import com.shverma.kinetic.ui.welcome.WelcomeScreen
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val activityProvider: AndroidActivityProvider by inject()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }

        setContent {
            AppNavigation()
        }
    }

    override fun onResume() {
        super.onResume()
        activityProvider.currentActivity = this
    }

    override fun onPause() {
        if (activityProvider.currentActivity === this) {
            activityProvider.currentActivity = null
        }
        super.onPause()
    }
}


@Composable
fun AppNavigation() {
    KineticApp(
        welcomeContent = { snackbarHostState, onNavigateToOnboarding, onNavigateToLanding ->
            WelcomeScreen(
                snackBarHostState = snackbarHostState,
                onNavigateToOnboarding = onNavigateToOnboarding,
                onNavigateToLanding = onNavigateToLanding,
            )
        },
        onboardingContent = { onFinish ->
            OnboardingScreen(onFinish = onFinish)
        },
        landingContent = { onLogout ->
            KineticLandingContent(
                fuelContent = { onAIChatClick ->
                    FuelScreen(onAIChatClick = onAIChatClick)
                },
                chatContent = { AIChatScreen() },
                profileContent = { ProfileScreen(onLogout = onLogout) },
                onLogout = onLogout,
            )
        }
    )
}
