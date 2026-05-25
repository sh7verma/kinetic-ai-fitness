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
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Sports
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shverma.kinetic.data.network.ChatType
import com.shverma.kinetic.ui.aichat.AIChatScreen
import com.shverma.kinetic.ui.coach.CoachScreen
import com.shverma.kinetic.ui.diet.CreateDietPlanScreen
import com.shverma.kinetic.ui.diet.DietPlanScreen
import com.shverma.kinetic.ui.fuel.FuelScreen
import com.shverma.kinetic.ui.logexercise.LogExerciseScreen
import com.shverma.kinetic.ui.plan.PlanScreen
import com.shverma.kinetic.ui.profile.ProfileScreen
import com.shverma.kinetic.ui.stats.StatsScreen
import com.shverma.kinetic.ui.theme.AppTheme
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily
import dagger.hilt.android.AndroidEntryPoint
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
    object Plan

    @Serializable
    object Fuel

    @Serializable
    object Stats

    @Serializable
    object Coach

    @Serializable
    object Profile

    @Serializable
    object LogExercise

    @Serializable
    data class AIChat(val chatType: String)

    @Serializable
    object DietPlan

    @Serializable
    object CreateDietPlan
}

@Composable
fun LandingScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showBottomBar = currentDestination?.hasRoute<LandingRoutes.LogExercise>() == false &&
            currentDestination?.hasRoute<LandingRoutes.AIChat>() == false &&
            currentDestination?.hasRoute<LandingRoutes.DietPlan>() == false &&
            currentDestination?.hasRoute<LandingRoutes.CreateDietPlan>() == false

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
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
            }
        },
        containerColor = Color(0xFF0E0E0E) // Pure Obsidian Black
    ) { padding ->
        val startDestination = if (BuildConfig.IS_DEBUG) LandingRoutes.Plan else LandingRoutes.Fuel

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) padding.calculateBottomPadding() else 0.dp) // Avoid extra padding if screens have their own Scaffold
        ) {
            composable<LandingRoutes.Plan> {
                PlanScreen(
                    onLogComplete = {
                        navController.navigate(LandingRoutes.LogExercise)
                    }
                )
            }
            composable<LandingRoutes.Fuel> {
                FuelScreen(
                    onAIChatClick = {
                        navController.navigate(LandingRoutes.AIChat(chatType = ChatType.MEALS.name))
                    },
                    onAddMealClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Manual meal entry — coming soon!")
                        }
                    },
                    onEnergyCardClick = {
                        navController.navigate(LandingRoutes.DietPlan)
                    }
                )
            }
            composable<LandingRoutes.Stats> { StatsScreen() }
            composable<LandingRoutes.Coach> {
                CoachScreen(
                    onMealChatClick = {
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
            composable<LandingRoutes.LogExercise> {
                LogExerciseScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable<LandingRoutes.AIChat> { backStackEntry ->
                val args = backStackEntry.toRoute<LandingRoutes.AIChat>()
                AIChatScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable<LandingRoutes.DietPlan> {
                DietPlanScreen(
                    onBackClick = { navController.popBackStack() },
                    onCreatePlanClick = {
                        navController.navigate(LandingRoutes.CreateDietPlan)
                    }
                )
            }
            composable<LandingRoutes.CreateDietPlan> {
                CreateDietPlanScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun KineticBottomNavigation(
    currentDestination: NavDestination?,
    onTabSelected: (Any) -> Unit
) {
    val tabs = remember {
        val allTabs = listOf(
            NavigationItem("Plan", Icons.Outlined.EventNote, LandingRoutes.Plan),
            NavigationItem("Fuel", Icons.Outlined.LocalFireDepartment, LandingRoutes.Fuel),
            NavigationItem("Stats", Icons.Outlined.BarChart, LandingRoutes.Stats),
            NavigationItem("Coach", Icons.Outlined.Sports, LandingRoutes.Coach),
            NavigationItem("Profile", Icons.Outlined.Person, LandingRoutes.Profile)
        )
        if (BuildConfig.IS_DEBUG) {
            allTabs
        } else {
            allTabs.filter { it.route == LandingRoutes.Fuel || it.route == LandingRoutes.Profile }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color(0xE6131313)) // bg-[#131313]/90
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
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

    val activeColor = Color(0xFFCCFF00)
    val inactiveColor = Color(0xFF737373).copy(alpha = 0.6f)

    val contentColor = if (isActive) activeColor else inactiveColor

    Column(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
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
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        letterSpacing = 2.sp // tracking-widest
                    ),
                    color = contentColor
                )
            }
        }
    }
}
