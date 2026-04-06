package com.shverma.app

import android.annotation.SuppressLint
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
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shverma.app.ui.details.DetailScreen
import com.shverma.app.ui.home.HomeScreen
import com.shverma.app.ui.theme.AppTheme
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
            isAppearanceLightStatusBars = true
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HomeRoute,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable<Routes.HomeRoute> {
                HomeScreen(
                    snackBarHostState = snackBarHostState,
                    onItemClick = { itemId ->
                        navController.navigate(Routes.DetailRoute(itemId))
                    }
                )
            }

            composable<Routes.DetailRoute> { navBackStackEntry ->
                val args = navBackStackEntry.toRoute<Routes.DetailRoute>()
                DetailScreen(
                    itemId = args.id,
                    snackBarHostState = snackBarHostState,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}


object Routes {

    @Serializable
    object HomeRoute

    @Serializable
    data class DetailRoute(val id: Int)
}