package com.shverma.kinetic.ui.onboarding

import com.shverma.kinetic.data.model.ai.NutritionStrategy
import com.shverma.kinetic.data.model.ai.TargetCaloriesData
import kotlinx.coroutines.flow.StateFlow

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.BIOMETRICS,
    val totalSteps: Int = OnboardingStep.totalSteps,
    val age: Double = 28.0,
    val weight: Double = 75.0,
    val weightUnit: String = "KG",
    val height: Double = 175.0,
    val heightUnit: String = "CM",
    val sex: String = "MALE",
    val workoutGoal: String = "Maintain",
    val selectedActivityLevel: String = "ACTIVE",
    val targetCaloriesData: TargetCaloriesData? = null,
    val nutritionStrategy: NutritionStrategy? = null,
    val isLoading: Boolean = false,
)

interface OnboardingActions {
    val uiState: StateFlow<OnboardingUiState>

    fun saveUserProfileData(onComplete: (() -> Unit)? = null)
    fun nextStep()
    fun updateAge(age: Double)
    fun updateWeight(weight: Double, unit: String)
    fun updateHeight(height: Double, unit: String)
    fun updateSex(sex: String)
    fun updateWorkoutGoal(goal: String)
    fun updateActivityLevel(level: String)
}
