package com.shverma.kinetic.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.auth.AuthSession
import com.shverma.kinetic.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.StateFlow

/** Android lifecycle/auth adapter for the shared onboarding controller. */
class OnboardingViewModel(
    userProfileRepository: UserProfileRepository,
    authSession: AuthSession,
) : ViewModel(), OnboardingActions {
    private val controller = OnboardingController(
        userProfileRepository = userProfileRepository,
        currentUserId = { authSession.currentUser()?.uid.orEmpty() },
        scope = viewModelScope,
    )

    override val uiState: StateFlow<OnboardingUiState> = controller.uiState

    override fun saveUserProfileData(onComplete: (() -> Unit)?) =
        controller.saveUserProfileData(onComplete)

    override fun nextStep() = controller.nextStep()

    override fun updateAge(age: Double) = controller.updateAge(age)

    override fun updateWeight(weight: Double, unit: String) = controller.updateWeight(weight, unit)

    override fun updateHeight(height: Double, unit: String) = controller.updateHeight(height, unit)

    override fun updateSex(sex: String) = controller.updateSex(sex)

    override fun updateWorkoutGoal(goal: String) = controller.updateWorkoutGoal(goal)

    override fun updateActivityLevel(level: String) = controller.updateActivityLevel(level)
}
