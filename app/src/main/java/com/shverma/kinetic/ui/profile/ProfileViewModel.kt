package com.shverma.kinetic.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.auth.GoogleAuthRepository
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.data.model.UserProfileData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: GoogleAuthRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfileData?>(null)
    val userProfile: StateFlow<UserProfileData?> = _userProfile.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userProfileRepository.getUserProfileData().collect { data ->
                _userProfile.value = data
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            userProfileRepository.clearUserProfileData()
            _events.emit(ProfileEvent.LogoutSuccess)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            user?.let {
                userProfileRepository.deleteUserFromFirestore(it.uid)
                authRepository.deleteAccount()
                userProfileRepository.clearUserProfileData()
                _events.emit(ProfileEvent.DeleteAccountSuccess)
            }
        }
    }
}

sealed class ProfileEvent {
    data object LogoutSuccess : ProfileEvent()
    data object DeleteAccountSuccess : ProfileEvent()
}
