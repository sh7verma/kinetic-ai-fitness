package com.shverma.kinetic.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.auth.GoogleAuthRepository
import com.shverma.kinetic.data.model.DietaryGoal
import com.shverma.kinetic.data.model.UserProfileData
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
    // Biometrics
    val age: Double = 28.0,
    val weight: Double = 75.0,
    val weightUnit: String = "KG",
    val height: Double = 175.0,
    val heightUnit: String = "CM",
    val sex: String = "MALE",
    // Workout Setup
    val workoutGoal: String = "FAT LOSS",
    val commitmentDays: Double = 4.0,
    val equipment: String = "FULL GYM",
    // Meal Setup Data
    val selectedGoal: DietaryGoal = DietaryGoal.MUSCLE_GAIN,
    val selectedDietTypes: List<String> = listOf("BALANCED"),
    val selectedAllergies: List<String> = emptyList(),
    // Activity Level Data
    val selectedActivityLevel: String = "ACTIVE",
    // Cuisine Preference Data
    val selectedCuisines: List<String> = listOf("MEDITERRANEAN")
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
                            commitmentDays = savedData.commitmentDays,
                            equipment = savedData.equipment,
                            selectedGoal = savedData.dietaryGoal,
                            selectedDietTypes = savedData.dietTypes,
                            selectedAllergies = savedData.allergies,
                            selectedActivityLevel = savedData.activityLevel,
                            selectedCuisines = savedData.cuisines,
                            currentStep = if (savedData.isCompleted) OnboardingStep.entries.last() else state.currentStep
                        )
                    }
                }
                isDataLoaded = true
            }
        }
    }

    fun saveUserProfileData() {
        if (!isDataLoaded) return
        viewModelScope.launch {
            val state = _uiState.value
            val currentUser = authRepository.getCurrentUser()
            val data = UserProfileData(
                uid = currentUser?.uid ?: "",
                age = state.age,
                weight = state.weight,
                weightUnit = state.weightUnit,
                height = state.height,
                heightUnit = state.heightUnit,
                sex = state.sex,
                workoutGoal = state.workoutGoal,
                commitmentDays = state.commitmentDays,
                equipment = state.equipment,
                dietaryGoal = state.selectedGoal,
                dietTypes = state.selectedDietTypes,
                allergies = state.selectedAllergies,
                activityLevel = state.selectedActivityLevel,
                cuisines = state.selectedCuisines,
                isCompleted = state.currentStep == OnboardingStep.entries.last()
            )
            userProfileRepository.saveUserProfileData(data)
            userProfileRepository.saveUserProfileToFirestore(data)
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

    fun previousStep() {
        val currentStepNumber = _uiState.value.currentStep.stepNumber
        if (currentStepNumber > 1) {
            val prevStep = OnboardingStep.fromStepNumber(currentStepNumber - 1)
            _uiState.update { it.copy(currentStep = prevStep) }
        }
    }

    fun skipOnboarding() {
        // Handle skip logic - might navigate to landing activity
        _uiState.update { it.copy(currentStep = OnboardingStep.entries.last()) }
        saveUserProfileData()
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

    // Workout Actions
    fun updateWorkoutGoal(goal: String) {
        _uiState.update { it.copy(workoutGoal = goal) }
    }

    fun updateCommitmentDays(days: Double) {
        _uiState.update { it.copy(commitmentDays = days) }
    }

    fun updateEquipment(equipment: String) {
        _uiState.update { it.copy(equipment = equipment) }
    }

    // Meal Setup Actions
    fun updateDietaryGoal(goal: DietaryGoal) {
        _uiState.update { it.copy(selectedGoal = goal) }
    }

    fun toggleDietType(key: String) {
        _uiState.update { state ->
            val currentTypes = state.selectedDietTypes
            val newTypes = if (key == "BALANCED") {
                listOf("BALANCED")
            } else {
                val mutable = currentTypes.toMutableSet().also {
                    it.remove("BALANCED")
                    if (it.contains(key)) it.remove(key) else it.add(key)
                }
                if (mutable.isEmpty()) listOf("BALANCED") else mutable.toList()
            }
            state.copy(selectedDietTypes = newTypes)
        }
    }

    fun toggleAllergy(allergy: String) {
        _uiState.update { state ->
            val currentAllergies = state.selectedAllergies
            val newAllergies = if (currentAllergies.contains(allergy)) {
                currentAllergies.filter { it != allergy }
            } else {
                currentAllergies + allergy
            }
            state.copy(selectedAllergies = newAllergies)
        }
    }

    // Activity & Cuisine Actions
    fun updateActivityLevel(level: String) {
        _uiState.update { it.copy(selectedActivityLevel = level) }
    }

    fun toggleCuisine(cuisine: String) {
        _uiState.update { state ->
            val currentCuisines = state.selectedCuisines
            val newCuisines = if (currentCuisines.contains(cuisine)) {
                currentCuisines.filter { it != cuisine }
            } else {
                currentCuisines + cuisine
            }
            state.copy(selectedCuisines = newCuisines)
        }
    }
}
