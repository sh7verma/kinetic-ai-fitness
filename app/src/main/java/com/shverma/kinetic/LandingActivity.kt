package com.shverma.kinetic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import com.shverma.kinetic.ui.theme.AppTheme
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily
import com.shverma.kinetic.ui.plan.PlanScreen
import com.shverma.kinetic.ui.fuel.FuelScreen
import com.shverma.kinetic.ui.stats.StatsScreen
import com.shverma.kinetic.ui.coach.CoachScreen
import com.shverma.kinetic.ui.profile.ProfileScreen
import com.shverma.kinetic.ui.logexercise.LogExerciseScreen
import com.shverma.kinetic.ui.aichat.AIChatScreen
import com.shverma.kinetic.ui.fuel.LogMealsScreen
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
    @Serializable object Plan
    @Serializable object Fuel
    @Serializable object Stats
    @Serializable object Coach
    @Serializable object Profile
    @Serializable object LogExercise
    @Serializable object AIChat
    @Serializable object LogMeals
    @Serializable object TodayMealPlan
}

@Composable
fun LandingScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.hasRoute<LandingRoutes.LogExercise>() == false &&
            currentDestination?.hasRoute<LandingRoutes.AIChat>() == false &&
            currentDestination?.hasRoute<LandingRoutes.LogMeals>() == false &&
            currentDestination?.hasRoute<LandingRoutes.TodayMealPlan>() == false

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
        NavHost(
            navController = navController,
            startDestination = LandingRoutes.Plan,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()) // Avoid extra padding if screens have their own Scaffold
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
                        navController.navigate(LandingRoutes.AIChat)
                    },
                    onAddMealClick = {
                        navController.navigate(LandingRoutes.LogMeals)
                    },
                    onEnergyCardClick = {
                        navController.navigate(LandingRoutes.TodayMealPlan)
                    }
                ) 
            }
            composable<LandingRoutes.Stats> { StatsScreen() }
            composable<LandingRoutes.Coach> { CoachScreen() }
            composable<LandingRoutes.Profile> { ProfileScreen() }
            composable<LandingRoutes.LogExercise> { 
                LogExerciseScreen(
                    onBackClick = { navController.popBackStack() }
                ) 
            }
            composable<LandingRoutes.AIChat> {
                AIChatScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable<LandingRoutes.LogMeals> {
                LogMealsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable<LandingRoutes.TodayMealPlan> {
                com.shverma.kinetic.ui.fuel.TodayMealPlanScreen(
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
    val tabs = listOf(
        NavigationItem("Plan", Icons.Outlined.EventNote, LandingRoutes.Plan),
        NavigationItem("Fuel", Icons.Outlined.LocalFireDepartment, LandingRoutes.Fuel),
        NavigationItem("Stats", Icons.Outlined.BarChart, LandingRoutes.Stats),
        NavigationItem("Coach", Icons.Outlined.Sports, LandingRoutes.Coach),
        NavigationItem("Profile", Icons.Outlined.Person, LandingRoutes.Profile)
    )

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
