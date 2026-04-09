package com.shverma.kinetic.ui.onboarding.screens

import com.shverma.kinetic.ui.onboarding.OnboardingStep
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel
import com.shverma.kinetic.ui.onboarding.components.OnboardingBottomNavigation
import com.shverma.kinetic.ui.onboarding.components.OnboardingStepHeader

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shverma.kinetic.ui.theme.*

@Composable
fun OnboardingActivityStep(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit = {},
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            OnboardingStepHeader(
                title = "ACTIVITY & CUISINE",
                subtitle = "LIFESTYLE SYNC",
                description = "Integrate your daily activity levels and culinary preferences for accurate tracking."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Activity Level Section
            ActivityLevelSection(
                selectedLevel = uiState.selectedActivityLevel,
                onLevelSelected = { viewModel.updateActivityLevel(it) }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Syncing footer
            SyncingFooter()
        }

        // Bottom Navigation Buttons
        OnboardingBottomNavigation(
            onContinue = onContinue,
            onBack = onBack,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SectionHeaderReusable(
    title: String,
    rightText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .background(MealVolt)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title.uppercase(),
                style = LocalKineticTypography.current.labelLg.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }
        if (rightText != null) {
            Text(
                text = rightText.uppercase(),
                style = LocalKineticTypography.current.labelSm.copy(
                    color = MealGray,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

data class ActivityLevel(
    val id: String,
    val levelLabel: String,
    val title: String,
    val description: String,
    val isRecommended: Boolean = false,
    val color: Color = MealVolt
)

val activityLevels = listOf(
    ActivityLevel("SEDENTARY", "LVL 01", "SEDENTARY", "Office job, minimal physical activity in daily routine.", color = KineticSecondary),
    ActivityLevel("MODERATE", "LVL 02", "MODERATE", "30–60 mins of moderate activity 3–4 times a week.", color = KineticTertiary),
    ActivityLevel("ACTIVE", "LVL 03", "ACTIVE", "Daily intentional exercise and high step count goals.", isRecommended = true, color = MealVolt),
    ActivityLevel("PRO-ATHLETE", "LVL 04", "PRO-ATHLETE", "Intense training sessions multiple times daily.", color = KineticTertiary)
)

@Composable
fun ActivityLevelSection(
    selectedLevel: String,
    onLevelSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeaderReusable(title = "ACTIVITY LEVEL")
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            activityLevels.forEach { level ->
                ActivityLevelCard(
                    level = level,
                    isSelected = selectedLevel == level.id,
                    onClick = { onLevelSelected(level.id) }
                )
            }
        }
    }
}

@Composable
fun ActivityLevelCard(
    level: ActivityLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accentColor = level.color

    val borderColor = if (isSelected) accentColor else Color.Transparent
    val titleColor = if (isSelected) accentColor else Color.White
    val labelColor = if (isSelected) accentColor else MealGray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MealCard)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // Ghost Icon
        Icon(
            imageVector = Icons.Default.Person, // Use Person as placeholder for ghost icon
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.TopEnd)
                .alpha(0.07f)
        )

        Column {
            Text(
                text = if (level.isRecommended && isSelected) "${level.levelLabel} / RECOMMENDED" else level.levelLabel,
                style = LocalKineticTypography.current.labelSm.copy(
                    color = labelColor,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = level.title,
                style = LocalKineticTypography.current.titleLg.copy(
                    color = titleColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold // weight 800
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = level.description,
                style = LocalKineticTypography.current.bodySm.copy(
                    color = MealDescription,
                    fontSize = 13.sp,
                    lineHeight = 19.sp // 1.5 * 13
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Text(
                        text = "ACTIVE SELECTION",
                        style = LocalKineticTypography.current.labelSm.copy(
                            color = accentColor,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)) // Filled green circle dot
                    )
                } else {
                    Text(
                        text = "SELECT",
                        style = LocalKineticTypography.current.labelSm.copy(
                            color = accentColor.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SyncingFooter() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .alpha(alpha)
                .clip(CircleShape)
                .background(SyncDot)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "SYNCING PREFERENCES WITH BIOMETRIC CORE...",
            style = LocalKineticTypography.current.labelSm.copy(
                color = SyncText,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
