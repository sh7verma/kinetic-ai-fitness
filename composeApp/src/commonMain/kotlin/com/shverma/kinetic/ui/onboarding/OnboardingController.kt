package com.shverma.kinetic.ui.onboarding

import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.repository.MacrosCalculator
import com.shverma.kinetic.data.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Shared onboarding state machine; platform code supplies scope and current-user identity. */
class OnboardingController(
    private val userProfileRepository: UserProfileRepository,
    private val currentUserId: () -> String,
    private val scope: CoroutineScope,
) : OnboardingActions {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    override val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var isDataLoaded = false

    init {
        loadUserProfileData()
    }

    private fun loadUserProfileData() {
        scope.launch {
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
                            currentStep = if (savedData.isCompleted) {
                                OnboardingStep.entries.last()
                            } else {
                                state.currentStep
                            },
                        )
                    }
                }
                isDataLoaded = true
            }
        }
    }

    override fun saveUserProfileData(onComplete: (() -> Unit)?) {
        if (!isDataLoaded) return
        _uiState.update { it.copy(isLoading = true) }
        scope.launch {
            try {
                val state = _uiState.value
                val initialData = UserProfileData(
                    uid = currentUserId(),
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
                    isCompleted = state.currentStep == OnboardingStep.entries.last(),
                )

                val finalUser = if (initialData.isCompleted && initialData.targetCaloriesData == null) {
                    val result = userProfileRepository.getInitialTargetCalories(initialData)
                    val updatedData = if (result != null) {
                        val (aiTargets, strategy) = result
                        initialData.copy(targetCaloriesData = aiTargets, nutritionStrategy = strategy)
                    } else {
                        initialData.copy(targetCaloriesData = MacrosCalculator.fallback(initialData))
                    }
                    _uiState.update {
                        it.copy(
                            targetCaloriesData = updatedData.targetCaloriesData,
                            nutritionStrategy = updatedData.nutritionStrategy,
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

    override fun nextStep() {
        val currentStepNumber = _uiState.value.currentStep.stepNumber
        saveUserProfileData()
        if (currentStepNumber < OnboardingStep.totalSteps) {
            val nextStep = OnboardingStep.fromStepNumber(currentStepNumber + 1)
            _uiState.update { it.copy(currentStep = nextStep) }
        }
    }

    override fun updateAge(age: Double) {
        _uiState.update { it.copy(age = age) }
    }

    override fun updateWeight(weight: Double, unit: String) {
        _uiState.update { it.copy(weight = weight, weightUnit = unit) }
    }

    override fun updateHeight(height: Double, unit: String) {
        _uiState.update { it.copy(height = height, heightUnit = unit) }
    }

    override fun updateSex(sex: String) {
        _uiState.update { it.copy(sex = sex) }
    }

    override fun updateWorkoutGoal(goal: String) {
        _uiState.update { it.copy(workoutGoal = goal) }
    }

    override fun updateActivityLevel(level: String) {
        _uiState.update { it.copy(selectedActivityLevel = level) }
    }
}
