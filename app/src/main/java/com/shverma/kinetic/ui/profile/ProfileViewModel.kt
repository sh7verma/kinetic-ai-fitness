package com.shverma.kinetic.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.auth.GoogleAuthRepository
import com.shverma.kinetic.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: GoogleAuthRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            userProfileRepository.clearUserProfileData()
            _events.emit(ProfileEvent.LogoutSuccess)
        }
    }
}

sealed class ProfileEvent {
    data object LogoutSuccess : ProfileEvent()
}
