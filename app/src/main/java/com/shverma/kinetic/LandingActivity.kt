package com.shverma.kinetic

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shverma.kinetic.data.network.ChatType
import com.shverma.kinetic.ui.aichat.AIChatScreen
import com.shverma.kinetic.ui.fuel.FuelScreen
import com.shverma.kinetic.ui.profile.ProfileScreen
import com.shverma.kinetic.ui.theme.AppTheme
import com.shverma.kinetic.ui.theme.KineticTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class LandingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                LandingScreen()
            }
        }
    }
}

object LandingRoutes {
    @Serializable
    object Fuel

    @Serializable
    object Profile

    @Serializable
    data class AIChat(val chatType: String)
}

@Composable
fun LandingScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colors = KineticTheme.colors

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            KineticBottomNavigation(
                currentDestination = currentDestination,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        containerColor = colors.background
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = LandingRoutes.Fuel,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            composable<LandingRoutes.Fuel> {
                FuelScreen(
                    onAIChatClick = {
                        navController.navigate(LandingRoutes.AIChat(chatType = ChatType.MEALS.name))
                    }
                )
            }
            composable<LandingRoutes.Profile> {
                val context = LocalContext.current
                ProfileScreen(
                    onLogout = {
                        (context as? Activity)?.let { activity ->
                            val intent = android.content.Intent(activity, MainActivity::class.java)
                            activity.startActivity(intent)
                            activity.finish()
                        }
                    }
                )
            }
            composable<LandingRoutes.AIChat> {
                AIChatScreen()
            }
        }
    }
}

@Composable
fun KineticBottomNavigation(
    currentDestination: NavDestination?,
    onTabSelected: (Any) -> Unit
) {
    val colors = KineticTheme.colors
    val tabs = remember {
        listOf(
            NavigationItem("Log", Icons.AutoMirrored.Outlined.Chat, LandingRoutes.AIChat(chatType = ChatType.MEALS.name)),
            NavigationItem("Fuel", Icons.Outlined.LocalFireDepartment, LandingRoutes.Fuel),
            NavigationItem("Profile", Icons.Outlined.Person, LandingRoutes.Profile),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(colors.surfaceContainer)
            .border(
                width = 1.dp,
                color = colors.outlineVariant.copy(alpha = 0.09f),
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { item ->
                val isActive = currentDestination?.hasRoute(item.route::class) ?: false
                KineticNavigationItem(
                    item = item,
                    isActive = isActive,
                    onClick = { onTabSelected(item.route) }
                )
            }
        }
    }
}

data class NavigationItem(val label: String, val icon: ImageVector, val route: Any)

@Composable
fun KineticNavigationItem(
    item: NavigationItem,
    isActive: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")
    val colors = KineticTheme.colors

    val activeColor = colors.primary
    val inactiveColor = colors.onSurfaceVariant

    val contentColor = if (isActive) activeColor else inactiveColor

    Column(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        tryAwaitRelease()
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
            .scale(scale)
            .width(74.dp)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isActive) activeColor.copy(alpha = 0.1f) else Color.Transparent)
                .padding(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = item.label.uppercase(),
                    style = KineticTheme.typography.labelSm,
                    color = contentColor
                )
            }
        }
    }
}
