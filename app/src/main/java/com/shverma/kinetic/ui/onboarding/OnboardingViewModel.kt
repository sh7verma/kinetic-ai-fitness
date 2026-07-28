package com.shverma.kinetic.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.auth.GoogleAuthRepository
import com.shverma.kinetic.data.model.ai.NutritionStrategy
import com.shverma.kinetic.data.model.ai.TargetCaloriesData
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.repository.MacrosCalculator
import com.shverma.kinetic.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.BIOMETRICS,
    val totalSteps: Int = OnboardingStep.totalSteps,
    // Biometrics — feeds the Mifflin-St Jeor BMR calculation directly
    val age: Double = 28.0,
    val weight: Double = 75.0,
    val weightUnit: String = "KG",
    val height: Double = 175.0,
    val heightUnit: String = "CM",
    val sex: String = "MALE",
    // Goal & Activity — goal feeds the AI strategy prompt, activity feeds the
    // TDEE multiplier directly (see MacrosCalculator.calculate)
    val workoutGoal: String = "Maintain",
    val selectedActivityLevel: String = "ACTIVE",
    // AI-calculated targets, shown on the Results step
    val targetCaloriesData: TargetCaloriesData? = null,
    val nutritionStrategy: NutritionStrategy? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val authRepository: GoogleAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var isDataLoaded = false

    init {
        loadUserProfileData()
    }

    private fun loadUserProfileData() {
        viewModelScope.launch {
            userProfileRepository.getUserProfileData().collect { data ->
                data?.let { savedData ->
                    _uiState.update { state ->
                        state.copy(
                            age = savedData.age,
                            weight = savedData.weight,
                            weightUnit = savedData.weightUnit,
                            height = savedData.height,
                            heightUnit = savedData.heightUnit,
                            sex = savedData.sex,
                            workoutGoal = savedData.workoutGoal,
                            selectedActivityLevel = savedData.activityLevel,
                            targetCaloriesData = savedData.targetCaloriesData,
                            nutritionStrategy = savedData.nutritionStrategy,
                            currentStep = if (savedData.isCompleted) OnboardingStep.entries.last() else state.currentStep
                        )
                    }
                }
                isDataLoaded = true
            }
        }
    }

    fun saveUserProfileData(onComplete: (() -> Unit)? = null) {
        if (!isDataLoaded) return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val currentUser = authRepository.getCurrentUser()
                val initialData = UserProfileData(
                    uid = currentUser?.uid ?: "",
                    age = state.age,
                    weight = state.weight,
                    weightUnit = state.weightUnit,
                    height = state.height,
                    heightUnit = state.heightUnit,
                    sex = state.sex,
                    workoutGoal = state.workoutGoal,
                    activityLevel = state.selectedActivityLevel,
                    targetCaloriesData = state.targetCaloriesData,
                    nutritionStrategy = state.nutritionStrategy,
                    isCompleted = state.currentStep == OnboardingStep.entries.last()
                )

                val finalUser = if (initialData.isCompleted && initialData.targetCaloriesData == null) {
                    val result = userProfileRepository.getInitialTargetCalories(initialData)
                    val updatedData = if (result != null) {
                        val (aiTargets, strategy) = result
                        initialData.copy(targetCaloriesData = aiTargets, nutritionStrategy = strategy)
                    } else {
                        // AI call failed — fall back rather than leaving the user stuck with no targets.
                        initialData.copy(targetCaloriesData = MacrosCalculator.fallback(initialData))
                    }
                    _uiState.update {
                        it.copy(
                            targetCaloriesData = updatedData.targetCaloriesData,
                            nutritionStrategy = updatedData.nutritionStrategy
                        )
                    }
                    updatedData
                } else {
                    initialData
                }

                userProfileRepository.saveUserProfileData(finalUser)
                userProfileRepository.saveUserProfileToFirestore(finalUser)
                onComplete?.invoke()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun nextStep() {
        val currentStepNumber = _uiState.value.currentStep.stepNumber
        saveUserProfileData()
        if (currentStepNumber < OnboardingStep.totalSteps) {
            val nextStep = OnboardingStep.fromStepNumber(currentStepNumber + 1)
            _uiState.update { it.copy(currentStep = nextStep) }
        }
    }

    // Biometrics Actions
    fun updateAge(age: Double) {
        _uiState.update { it.copy(age = age) }
    }

    fun updateWeight(weight: Double, unit: String) {
        _uiState.update { it.copy(weight = weight, weightUnit = unit) }
    }

    fun updateHeight(height: Double, unit: String) {
        _uiState.update { it.copy(height = height, heightUnit = unit) }
    }

    fun updateSex(sex: String) {
        _uiState.update { it.copy(sex = sex) }
    }

    // Goal & Activity Actions
    fun updateWorkoutGoal(goal: String) {
        _uiState.update { it.copy(workoutGoal = goal) }
    }

    fun updateActivityLevel(level: String) {
        _uiState.update { it.copy(selectedActivityLevel = level) }
    }
}
