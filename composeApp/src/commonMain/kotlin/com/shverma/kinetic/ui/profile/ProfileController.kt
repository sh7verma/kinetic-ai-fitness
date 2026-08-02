package com.shverma.kinetic.ui.profile

import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.repository.MacrosCalculator
import com.shverma.kinetic.data.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Shared profile orchestration; platform auth SDK calls are supplied by adapters. */
class ProfileController(
    private val userProfileRepository: UserProfileRepository,
    private val currentUserId: () -> String?,
    private val signOut: suspend () -> Unit,
    private val deleteAuthAccount: suspend () -> Unit,
    private val scope: CoroutineScope,
) : ProfileActions {
    private val _userProfile = MutableStateFlow<UserProfileData?>(null)
    override val userProfile: StateFlow<UserProfileData?> = _userProfile.asStateFlow()

    private val _isRecalculating = MutableStateFlow(false)
    override val isRecalculating: StateFlow<Boolean> = _isRecalculating.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    override val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        scope.launch {
            userProfileRepository.getUserProfileData().collect { data ->
                _userProfile.value = data
            }
        }
    }

    override fun saveEditedTargets(updated: UserProfileData) {
        scope.launch {
            _isRecalculating.value = true
            try {
                val result = userProfileRepository.getInitialTargetCalories(updated)
                val finalProfile = if (result != null) {
                    val (targets, strategy) = result
                    updated.copy(targetCaloriesData = targets, nutritionStrategy = strategy)
                } else {
                    updated.copy(targetCaloriesData = MacrosCalculator.fallback(updated))
                }
                userProfileRepository.saveUserProfileData(finalProfile)
                userProfileRepository.saveUserProfileToFirestore(finalProfile)
            } finally {
                _isRecalculating.value = false
            }
        }
    }

    override fun logout() {
        scope.launch {
            signOut()
            userProfileRepository.clearUserProfileData()
            _events.emit(ProfileEvent.LogoutSuccess)
        }
    }

    override fun deleteAccount() {
        scope.launch {
            currentUserId()?.let { uid ->
                userProfileRepository.deleteUserFromFirestore(uid)
                deleteAuthAccount()
                userProfileRepository.clearUserProfileData()
                _events.emit(ProfileEvent.DeleteAccountSuccess)
            }
        }
    }
}
