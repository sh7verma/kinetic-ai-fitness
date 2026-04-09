package com.shverma.kinetic.ui.onboarding.screens

import com.shverma.kinetic.ui.onboarding.OnboardingStep
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel
import com.shverma.kinetic.ui.onboarding.components.OnboardingStepHeader
import com.shverma.kinetic.ui.onboarding.components.OnboardingBottomNavigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.ui.theme.LocalKineticTypography
import com.shverma.kinetic.ui.theme.LexendFamily
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily
import com.shverma.kinetic.ui.theme.KineticSecondary
import com.shverma.kinetic.ui.theme.KineticTertiary
import com.shverma.kinetic.ui.theme.MealVolt

@Composable
fun OnboardingBiometricsStep(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit = {},
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep = uiState.currentStep
    
    val age = uiState.age
    val weight = uiState.weight
    val height = uiState.height
    val sex = uiState.sex
    val weightUnit = uiState.weightUnit
    val heightUnit = uiState.heightUnit

    val typography = LocalKineticTypography.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OnboardingStepHeader(
                title = currentStep.title,
                subtitle = currentStep.subtitle,
                description = currentStep.description
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Age Card
                BiometricCard(
                    title = "AGE",
                    value = age.toInt().toString(),
                    unit = "YRS",
                    color = MealVolt
                ) {
                    KineticSlider(
                        value = age.toFloat(),
                        onValueChange = { viewModel.updateAge(it.toDouble()) },
                        valueRange = 14f..100f,
                        color = MealVolt
                    )
                }

                // Weight Card
                BiometricCard(
                    title = "WEIGHT",
                    value = weight.toInt().toString(),
                    unit = weightUnit,
                    color = KineticSecondary,
                    unitToggle = {
                        UnitToggle(
                            units = listOf("KG", "LBS"),
                            selectedUnit = weightUnit,
                            onUnitSelected = { viewModel.updateWeight(weight, it) },
                            color = KineticSecondary
                        )
                    }
                ) {
                    KineticSlider(
                        value = weight.toFloat(),
                        onValueChange = { viewModel.updateWeight(it.toDouble(), weightUnit) },
                        valueRange = 30f..200f,
                        color = KineticSecondary
                    )
                }

                // Height Card
                BiometricCard(
                    title = "HEIGHT",
                    value = height.toInt().toString(),
                    unit = heightUnit,
                    color = KineticTertiary,
                    unitToggle = {
                        UnitToggle(
                            units = listOf("CM", "FT"),
                            selectedUnit = heightUnit,
                            onUnitSelected = { viewModel.updateHeight(height, it) },
                            color = KineticTertiary
                        )
                    }
                ) {
                    KineticSlider(
                        value = height.toFloat(),
                        onValueChange = { viewModel.updateHeight(it.toDouble(), heightUnit) },
                        valueRange = 100f..250f,
                        color = KineticTertiary
                    )
                }

                // Sex Selection Card
                BiometricCard(title = "SEX", color = KineticSecondary) {
                    SexToggle(
                        selectedSex = sex,
                        onSexSelected = { viewModel.updateSex(it) },
                        color = KineticSecondary
                    )
                }

                Spacer(modifier = Modifier.height(100.dp)) // Space for bottom button
            }
        }


        // Bottom Navigation Buttons
        OnboardingBottomNavigation(
            onContinue = onContinue,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BiometricCard(
    title: String,
    value: String? = null,
    unit: String? = null,
    unitToggle: @Composable (() -> Unit)? = null,
    color: Color = KineticSecondary,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF131313))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PARAMETER",
                    style = LocalKineticTypography.current.labelSm.copy(
                        fontFamily = SpaceGroteskFamily,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = title,
                    style = LocalKineticTypography.current.titleMd.copy(
                        fontFamily = LexendFamily,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            if (unitToggle != null) {
                unitToggle()
            }
        }

        if (value != null) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = LocalKineticTypography.current.displayLg.copy(
                        fontFamily = LexendFamily,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 48.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = unit ?: "",
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = LocalKineticTypography.current.labelMd.copy(
                        fontFamily = SpaceGroteskFamily,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KineticSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    color: Color = KineticSecondary
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        thumb = {
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                colors = SliderDefaults.colors(
                    activeTrackColor = color,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.height(4.dp)
            )
        }
    )
}

@Composable
fun SexToggle(selectedSex: String, onSexSelected: (String) -> Unit, color: Color = KineticSecondary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SexOption(
            text = "MALE",
            isSelected = selectedSex == "MALE",
            modifier = Modifier.weight(1f),
            color = color,
            onClick = { onSexSelected("MALE") }
        )
        SexOption(
            text = "FEMALE",
            isSelected = selectedSex == "FEMALE",
            modifier = Modifier.weight(1f),
            color = color,
            onClick = { onSexSelected("FEMALE") }
        )
    }
}

@Composable
fun SexOption(text: String, isSelected: Boolean, modifier: Modifier, color: Color = KineticSecondary, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color else Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = LocalKineticTypography.current.bodyLg.copy(
                fontFamily = SpaceGroteskFamily,
                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun UnitToggle(units: List<String>, selectedUnit: String, onUnitSelected: (String) -> Unit, color: Color = KineticSecondary) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        units.forEach { unit ->
            val isSelected = unit == selectedUnit
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) color else Color.Transparent)
                    .clickable { onUnitSelected(unit) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit,
                    style = LocalKineticTypography.current.labelSm.copy(
                        fontFamily = SpaceGroteskFamily,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

