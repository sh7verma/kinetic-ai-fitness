package com.shverma.kinetic.ui.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shverma.kinetic.data.model.AllergyItem
import com.shverma.kinetic.data.model.allergyDatabase
import com.shverma.kinetic.ui.onboarding.OnboardingStep
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel
import com.shverma.kinetic.ui.onboarding.components.OnboardingBottomNavigation
import com.shverma.kinetic.ui.onboarding.components.OnboardingStepHeader
import com.shverma.kinetic.ui.theme.LocalKineticTypography
import com.shverma.kinetic.ui.theme.MealCard
import com.shverma.kinetic.ui.theme.MealDanger
import com.shverma.kinetic.ui.theme.MealGray
import com.shverma.kinetic.ui.theme.MealVolt
import com.shverma.kinetic.ui.theme.MealWhite
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CriticalSensitivitiesSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    allergies: List<AllergyItem>,
    selectedAllergies: List<String>,
    onToggleAllergy: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        MealSectionHeader(title = "CRITICAL SENSITIVITIES", accentColor = MealDanger)

        if (selectedAllergies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedAllergies.forEach { allergy ->
                    SelectedAllergyChip(
                        name = allergy,
                        onRemove = { onToggleAllergy(allergy) }
                    )
                }
            }
        }

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
            val tagText = if (item.isCommon) "COMMON" else null
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
fun SelectedAllergyChip(name: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(MealDanger.copy(alpha = 0.15f))
            .border(1.dp, MealDanger.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
            .clickable { onRemove() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = LocalKineticTypography.current.labelMd.copy(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.SemiBold,
                color = MealWhite
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            tint = MealWhite.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
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
