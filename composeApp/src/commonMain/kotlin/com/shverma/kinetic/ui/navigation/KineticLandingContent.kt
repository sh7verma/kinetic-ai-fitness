package com.shverma.kinetic.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shverma.kinetic.ui.theme.KineticTheme

enum class KineticLandingTab(
    val label: String,
    val icon: ImageVector,
) {
    Log("Log", Icons.AutoMirrored.Outlined.Chat),
    Fuel("Fuel", Icons.Outlined.LocalFireDepartment),
    Profile("Profile", Icons.Outlined.Person),
}

@Composable
fun KineticLandingContent(
    fuelContent: @Composable (onAIChatClick: () -> Unit) -> Unit,
    chatContent: @Composable () -> Unit,
    profileContent: @Composable (onLogout: () -> Unit) -> Unit,
    onLogout: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(KineticLandingTab.Fuel) }
    val colors = KineticTheme.colors

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            KineticBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
        containerColor = colors.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            when (selectedTab) {
                KineticLandingTab.Log -> chatContent()
                KineticLandingTab.Fuel -> fuelContent { selectedTab = KineticLandingTab.Log }
                KineticLandingTab.Profile -> profileContent(onLogout)
            }
        }
    }
}

@Composable
private fun KineticBottomNavigation(
    selectedTab: KineticLandingTab,
    onTabSelected: (KineticLandingTab) -> Unit,
) {
    val colors = KineticTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(colors.surfaceContainer)
            .border(
                width = 1.dp,
                color = colors.outlineVariant.copy(alpha = 0.09f),
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KineticLandingTab.entries.forEach { tab ->
                KineticNavigationItem(
                    tab = tab,
                    isActive = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                )
            }
        }
    }
}

@Composable
private fun KineticNavigationItem(
    tab: KineticLandingTab,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "navigation-scale",
    )
    val colors = KineticTheme.colors
    val contentColor = if (isActive) colors.primary else colors.onSurfaceVariant

    Column(
        modifier = Modifier
            .scale(scale)
            .width(74.dp)
            .wrapContentHeight()
            .clickable {
                isPressed = true
                onClick()
                isPressed = false
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isActive) contentColor.copy(alpha = 0.1f) else Color.Transparent)
                .padding(12.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = tab.label.uppercase(),
                    style = KineticTheme.typography.labelSm,
                    color = contentColor,
                )
            }
        }
    }
}
