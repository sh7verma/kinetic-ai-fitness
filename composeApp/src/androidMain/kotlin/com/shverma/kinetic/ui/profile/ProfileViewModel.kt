package com.shverma.kinetic.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.auth.AuthSession
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.data.model.UserProfileData
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/** Android lifecycle/auth adapter for the shared profile controller. */
class ProfileViewModel(
    private val authSession: AuthSession,
    userProfileRepository: UserProfileRepository,
) : ViewModel(), ProfileActions {
    private val controller = ProfileController(
        userProfileRepository = userProfileRepository,
        currentUserId = { authSession.currentUser()?.uid },
        signOut = authSession::signOut,
        deleteAuthAccount = authSession::deleteAccount,
        scope = viewModelScope,
    )

    override val userProfile: StateFlow<UserProfileData?> = controller.userProfile
    override val isRecalculating: StateFlow<Boolean> = controller.isRecalculating
    override val events: SharedFlow<ProfileEvent> = controller.events

    override fun saveEditedTargets(updated: UserProfileData) = controller.saveEditedTargets(updated)

    override fun logout() = controller.logout()

    override fun deleteAccount() = controller.deleteAccount()
}
