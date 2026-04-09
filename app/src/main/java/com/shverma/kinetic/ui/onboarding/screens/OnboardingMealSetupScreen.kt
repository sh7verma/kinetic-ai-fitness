package com.shverma.kinetic.ui.onboarding.screens

import com.shverma.kinetic.data.model.AllergyItem
import com.shverma.kinetic.data.model.DietaryGoal
import com.shverma.kinetic.data.model.allergyDatabase
import com.shverma.kinetic.data.model.dietChips
import com.shverma.kinetic.ui.onboarding.OnboardingStep
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel
import com.shverma.kinetic.ui.onboarding.components.OnboardingBottomNavigation
import com.shverma.kinetic.ui.onboarding.components.OnboardingStepHeader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shverma.kinetic.ui.theme.*

// ─── Root Screen ──────────────────────────────────────────────────────────────

@Composable
fun OnboardingMealSetupStep(
    viewModel: OnboardingViewModel,
    currentStep: OnboardingStep,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredAllergies = remember(searchQuery) {
        if (searchQuery.isBlank()) allergyDatabase
        else allergyDatabase.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val typography = LocalKineticTypography.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // ── Dietary Goal ──────────────────────────────────────────────────
                DietaryGoalSection(
                    selectedGoal = uiState.selectedGoal,
                    onGoalSelected = { viewModel.updateDietaryGoal(it) }
                )

                // ── Diet Type & Restrictions ──────────────────────────────────────
                DietTypeSection(
                    selectedDietTypes = uiState.selectedDietTypes,
                    onToggleDietType = { viewModel.toggleDietType(it) }
                )

                // ── Critical Sensitivities ────────────────────────────────────────
                CriticalSensitivitiesSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    allergies = filteredAllergies,
                    selectedAllergies = uiState.selectedAllergies,
                    onToggleAllergy = { viewModel.toggleAllergy(it) }
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
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
fun DietaryGoalSection(
    selectedGoal: DietaryGoal,
    onGoalSelected: (DietaryGoal) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        MealSectionHeader(title = "DIETARY GOAL", accentColor = MealCyan)
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DietaryGoal.entries.forEach { goal ->
                GoalCard(
                    goal = goal,
                    isSelected = selectedGoal == goal,
                    onClick = { onGoalSelected(goal) }
                )
            }
        }
    }
}

@Composable
fun GoalCard(goal: DietaryGoal, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MealCard)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MealVolt else MealWhite.copy(alpha = 0.06f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MealVolt.copy(alpha = 0.15f)
                        else MealWhite.copy(alpha = 0.04f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = goal.icon,
                    contentDescription = null,
                    tint = if (isSelected) MealVolt else MealGray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.label,
                    style = LocalKineticTypography.current.titleMd.copy(
                        fontFamily = LexendFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MealVolt else MealWhite,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goal.description,
                    style = LocalKineticTypography.current.bodySm.copy(
                        fontFamily = SpaceGroteskFamily,
                        color = if (isSelected) MealWhite.copy(alpha = 0.75f) else MealGray,
                        lineHeight = 18.sp
                    )
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MealVolt),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

// ─── 5. Diet Type & Restrictions ─────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DietTypeSection(
    selectedDietTypes: Set<String>,
    onToggleDietType: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        MealSectionHeader(title = "DIET TYPE & RESTRICTIONS", accentColor = MealVolt)
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            dietChips.forEach { chip ->
                DietChipItem(
                    label = chip.label,
                    isSelected = selectedDietTypes.contains(chip.key),
                    onClick = { onToggleDietType(chip.key) }
                )
            }
        }
    }
}

@Composable
fun DietChipItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (isSelected) MealVolt else MealCard)
            .border(
                width = 1.dp,
                color = if (isSelected) MealVolt else MealWhite.copy(alpha = 0.1f),
                shape = RoundedCornerShape(100.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = LocalKineticTypography.current.labelMd.copy(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else MealWhite,
                letterSpacing = 0.8.sp
            )
        )
    }
}

// ─── 6. Critical Sensitivities ────────────────────────────────────────────────

@Composable
fun CriticalSensitivitiesSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    allergies: List<AllergyItem>,
    selectedAllergies: Set<String>,
    onToggleAllergy: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        MealSectionHeader(title = "CRITICAL SENSITIVITIES", accentColor = MealDanger)
        Spacer(modifier = Modifier.height(16.dp))

        AllergySearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MealCard),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            allergies.forEachIndexed { index, item ->
                AllergyRow(
                    item = item,
                    isActive = selectedAllergies.contains(item.name),
                    onToggle = { onToggleAllergy(item.name) },
                    showDivider = index < allergies.lastIndex
                )
            }

            if (allergies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matches found",
                        style = LocalKineticTypography.current.bodyMd.copy(
                            fontFamily = SpaceGroteskFamily,
                            color = MealGray
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AllergySearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MealCard)
            .border(1.dp, MealWhite.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MealGray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = LocalKineticTypography.current.bodyMd.copy(
                fontFamily = SpaceGroteskFamily,
                color = MealWhite
            ),
            cursorBrush = SolidColor(MealVolt),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search allergies (e.g. Peanuts, Dairy...)",
                        style = LocalKineticTypography.current.bodyMd.copy(
                            fontFamily = SpaceGroteskFamily,
                            color = MealGray
                        )
                    )
                }
                inner()
            }
        )
    }
}

@Composable
fun AllergyRow(
    item: AllergyItem,
    isActive: Boolean,
    onToggle: () -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isActive) MealDanger.copy(alpha = 0.06f) else Color.Transparent)
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (isActive) MealDanger else MealWhite.copy(alpha = 0.06f))
                    .border(
                        1.dp,
                        if (isActive) MealDanger else MealWhite.copy(alpha = 0.15f),
                        RoundedCornerShape(5.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MealWhite,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                style = LocalKineticTypography.current.bodyMd.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isActive) MealWhite else MealWhite.copy(alpha = 0.85f)
                )
            )

            // Tag
            val tagText = if (isActive) "ACTIVE" else if (item.isCommon) "COMMON" else null
            if (tagText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            if (isActive) MealDanger.copy(alpha = 0.18f)
                            else MealWhite.copy(alpha = 0.06f)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tagText,
                        style = LocalKineticTypography.current.labelSm.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MealDanger else MealGray,
                            letterSpacing = 0.6.sp
                        )
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MealWhite.copy(alpha = 0.06f)
            )
        }
    }
}

@Composable
fun MealSectionHeader(title: String, accentColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = LocalKineticTypography.current.labelLg.copy(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                color = MealWhite,
                letterSpacing = 1.2.sp
            )
        )
    }
}
