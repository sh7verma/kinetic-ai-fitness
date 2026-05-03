package com.shverma.kinetic.ui.onboarding.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shverma.kinetic.ui.theme.BarlowCondensedFamily
import com.shverma.kinetic.ui.theme.KineticPrimaryContainer
import com.shverma.kinetic.ui.theme.LexendFamily
import com.shverma.kinetic.ui.theme.LocalKineticTypography
import com.shverma.kinetic.ui.theme.MealBg
import com.shverma.kinetic.ui.theme.MealVolt
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily
import com.shverma.kinetic.ui.theme.KineticSecondary
import com.shverma.kinetic.ui.theme.KineticTertiary
import com.shverma.kinetic.ui.theme.MealCyan
import com.shverma.kinetic.ui.theme.MealDanger
import com.shverma.kinetic.ui.theme.PlusJakartaSansFamily

@Composable
fun OnboardingStepProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STEP ${currentStep.toString().padStart(2, '0')}/${
                    totalSteps.toString().padStart(2, '0')
                }",
                style = LocalKineticTypography.current.labelSm.copy(
                    fontFamily = SpaceGroteskFamily,
                    color = MealVolt,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                val isCompleted = index < currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) MealVolt else Color.White.copy(alpha = 0.1f)
                        )
                )
            }
        }
    }
}

@Composable
fun OnboardingStepHeader(
    title: String,
    subtitle: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = LocalKineticTypography.current.displayMd.copy(
                fontFamily = BarlowCondensedFamily,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = Color.White
            )
        )
        Text(
            text = subtitle,
            style = LocalKineticTypography.current.titleMd.copy(
                fontFamily = BarlowCondensedFamily,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = MealVolt,
                letterSpacing = 2.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = LocalKineticTypography.current.bodySm.copy(
                fontFamily = PlusJakartaSansFamily,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        )
    }
}

@Composable
fun OnboardingTopBar(
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    onSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MealBg.copy(alpha = 0.7f))
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Back button at start
            if (currentStep > 1) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            // Title in center
            Text(
                text = "KINETIC AI",
                style = LocalKineticTypography.current.titleMd.copy(
                    fontFamily = LexendFamily,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = MealVolt
                )
            )

            // Skip button at end
            if (currentStep < totalSteps) {
                TextButton(
                    onClick = { onSkip?.invoke() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = "SKIP",
                        style = LocalKineticTypography.current.labelMd.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingBottomNavigation(
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null,
    backText: String = "BACK",
    continueText: String = "CONTINUE",
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val typography = LocalKineticTypography.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Back Button
        if (onBack != null) {
            OutlinedButton(
                onClick = onBack,
                enabled = enabled,
                modifier = Modifier
                    .weight(0.4f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.8f),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = backText,
                    style = typography.titleMd.copy(
                        fontFamily = LexendFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }

        // Continue Button
        Button(
            onClick = onContinue,
            enabled = enabled,
            modifier = Modifier
                .weight(if (onBack != null) 0.6f else 1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCCFF00),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = continueText,
                    style = typography.titleMd.copy(
                        fontFamily = LexendFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
