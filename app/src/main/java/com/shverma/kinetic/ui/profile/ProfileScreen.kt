package com.shverma.kinetic.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.ui.components.KineticSecondaryButton
import com.shverma.kinetic.ui.onboarding.components.OnboardingContinueButton
import com.shverma.kinetic.ui.onboarding.screens.ACTIVITY_OPTIONS
import com.shverma.kinetic.ui.onboarding.screens.ActivityOptionRow
import com.shverma.kinetic.ui.onboarding.screens.GOALS
import com.shverma.kinetic.ui.onboarding.screens.NumberField
import com.shverma.kinetic.ui.onboarding.screens.SegmentOption
import com.shverma.kinetic.ui.onboarding.screens.SexOption
import com.shverma.kinetic.ui.theme.KineticShape
import com.shverma.kinetic.ui.theme.KineticSpacing
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.utils.formatGoalName
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography
    val userProfile by viewModel.userProfile.collectAsState()
    val isRecalculating by viewModel.isRecalculating.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.LogoutSuccess -> onLogout()
                is ProfileEvent.DeleteAccountSuccess -> onLogout()
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This action is permanent and will delete all your data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.confidenceLowText)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(KineticSpacing.lg),
    ) {
        Text(text = "Profile", style = typography.titleLg, color = colors.onSurface)
        Spacer(Modifier.height(KineticSpacing.xl))

        val profile = userProfile
        when {
            profile == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = KineticSpacing.xxl),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading profile...", color = colors.onSurfaceVariant)
                }
            }

            isEditing -> {
                EditTargetsForm(
                    profile = profile,
                    isSaving = isRecalculating,
                    onCancel = { isEditing = false },
                    onSave = { updated ->
                        viewModel.saveEditedTargets(updated)
                        isEditing = false
                    }
                )
            }

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(KineticSpacing.lg)) {
                    DailyTargetsCard(profile = profile, onEditClick = { isEditing = true })
                    BiometricsCard(profile = profile)

                    Spacer(Modifier.height(KineticSpacing.sm))
                    KineticSecondaryButton(
                        text = "Sign out",
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = KineticSpacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Delete account",
                            style = typography.labelLg,
                            color = colors.confidenceLowText,
                            modifier = Modifier.clickable { showDeleteConfirmation = true }
                        )
                        Text(
                            text = "Permanently removes your log and profile.",
                            style = typography.bodySm,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTargetsCard(profile: UserProfileData, onEditClick: () -> Unit) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography
    val targets = profile.targetCaloriesData

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KineticShape.card))
            .background(colors.surfaceContainer)
            .padding(KineticSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm)
    ) {
        Text(text = "DAILY TARGETS", style = typography.labelMd, color = colors.onSurfaceVariant)
        ProfileRow("Calories", if (targets != null) "${targets.targetCalories.roundToInt()} kcal" else "—")
        ProfileRow("Protein", if (targets != null) "${targets.proteinG.roundToInt()} g" else "—")
        ProfileRow("Carbs", if (targets != null) "${targets.carbsG.roundToInt()} g" else "—")
        ProfileRow("Fat", if (targets != null) "${targets.fatsG.roundToInt()} g" else "—")
        Text(
            text = "Edit targets",
            style = typography.labelLg,
            color = colors.primary,
            modifier = Modifier
                .clickable { onEditClick() }
                .padding(top = KineticSpacing.xs)
        )
    }
}

@Composable
private fun BiometricsCard(profile: UserProfileData) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KineticShape.card))
            .background(colors.surfaceContainer)
            .padding(KineticSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm)
    ) {
        Text(text = "BIOMETRICS", style = typography.labelMd, color = colors.onSurfaceVariant)
        ProfileRow("Age", "${profile.age.roundToInt()}")
        ProfileRow("Weight", "${profile.weight.roundToInt()} ${profile.weightUnit}")
        ProfileRow("Height", "${profile.height.roundToInt()} ${profile.heightUnit}")
        ProfileRow("Activity level", profile.activityLevel.formatGoalName())
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = typography.bodyMd, color = colors.onSurfaceVariant)
        Text(text = value, style = typography.bodyMd, color = colors.onSurface)
    }
}

@Composable
private fun EditTargetsForm(
    profile: UserProfileData,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: (UserProfileData) -> Unit,
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    var age by remember { mutableStateOf(profile.age) }
    var weight by remember { mutableStateOf(profile.weight) }
    var height by remember { mutableStateOf(profile.height) }
    var sex by remember { mutableStateOf(profile.sex) }
    var goal by remember { mutableStateOf(profile.workoutGoal) }
    var activityLevel by remember { mutableStateOf(profile.activityLevel) }

    Column(verticalArrangement = Arrangement.spacedBy(KineticSpacing.xl)) {
        Text(text = "Edit targets", style = typography.titleMd, color = colors.onSurface)

        Row(horizontalArrangement = Arrangement.spacedBy(KineticSpacing.md)) {
            NumberField(label = "Age", value = age, onValueChange = { age = it }, modifier = Modifier.weight(1f))
            NumberField(label = "Weight (kg)", value = weight, onValueChange = { weight = it }, modifier = Modifier.weight(1f))
        }
        NumberField(label = "Height (cm)", value = height, onValueChange = { height = it })

        Column(verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm)) {
            Text(text = "Sex", style = typography.labelLg, color = colors.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(KineticSpacing.sm),
            ) {
                SexOption("Male", sex == "MALE", { sex = "MALE" }, Modifier.weight(1f))
                SexOption("Female", sex == "FEMALE", { sex = "FEMALE" }, Modifier.weight(1f))
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm)) {
            Text(text = "Goal", style = typography.labelLg, color = colors.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(KineticSpacing.sm),
            ) {
                GOALS.forEach { g ->
                    SegmentOption(text = g, isSelected = goal == g, onClick = { goal = g }, modifier = Modifier.weight(1f))
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm)) {
            Text(text = "Activity level", style = typography.labelLg, color = colors.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm)) {
                ACTIVITY_OPTIONS.forEach { option ->
                    ActivityOptionRow(
                        label = option.label,
                        isSelected = activityLevel == option.key,
                        onClick = { activityLevel = option.key }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(KineticSpacing.md)) {
            KineticSecondaryButton(text = "Cancel", onClick = onCancel, modifier = Modifier.weight(1f))
            OnboardingContinueButton(
                text = if (isSaving) "Saving..." else "Save",
                enabled = !isSaving,
                onClick = {
                    onSave(
                        profile.copy(
                            age = age,
                            weight = weight,
                            height = height,
                            sex = sex,
                            workoutGoal = goal,
                            activityLevel = activityLevel,
                            targetCaloriesData = null,
                            nutritionStrategy = null,
                        )
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
