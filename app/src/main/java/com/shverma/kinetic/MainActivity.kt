package com.shverma.kinetic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shverma.kinetic.ui.onboarding.screens.OnboardingScreen
import com.shverma.kinetic.ui.theme.AppTheme
import com.shverma.kinetic.ui.welcome.WelcomeScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }

        setContent {
            val snackBarHostState =
                remember { SnackbarHostState() }

            AppTheme {
                AppNavigation(snackBarHostState)
            }
        }
    }
}


@Composable
fun AppNavigation(
    snackBarHostState: SnackbarHostState
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.WelcomeRoute,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable<Routes.WelcomeRoute> {
                WelcomeScreen(
                    snackBarHostState = snackBarHostState,
                    onNavigateToOnboarding = {
                        navController.navigate(Routes.OnboardingRoute)
                    },
                    onNavigateToLanding = {
                        val intent = Intent(context, LandingActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }
                )
            }

            composable<Routes.OnboardingRoute> {
                OnboardingScreen(
                    onFinish = {
                        val intent = Intent(context, LandingActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }

        }
    }
}


object Routes {

    @Serializable
    object WelcomeRoute

    @Serializable
    object OnboardingRoute
}