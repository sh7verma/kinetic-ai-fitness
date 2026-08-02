package com.shverma.kinetic.ui.profile

import com.shverma.kinetic.data.model.UserProfileData
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ProfileActions {
    val userProfile: StateFlow<UserProfileData?>
    val isRecalculating: StateFlow<Boolean>
    val events: SharedFlow<ProfileEvent>

    fun saveEditedTargets(updated: UserProfileData)
    fun logout()
    fun deleteAccount()
}

sealed class ProfileEvent {
    data object LogoutSuccess : ProfileEvent()
    data object DeleteAccountSuccess : ProfileEvent()
}
