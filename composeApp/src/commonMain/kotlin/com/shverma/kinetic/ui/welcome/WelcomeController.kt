package com.shverma.kinetic.ui.welcome

import com.shverma.kinetic.data.preference.KineticPreferences
import com.shverma.kinetic.data.repository.FoodRepository
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.utils.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WelcomeController(
    private val userProfileRepository: UserProfileRepository,
    private val foodRepository: FoodRepository,
    private val preferences: KineticPreferences,
    private val currentUser: () -> AuthUser?,
    private val scope: CoroutineScope,
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkLocalSession()
        syncFoodsIfNeeded()
    }

    private fun syncFoodsIfNeeded() {
        scope.launch {
            val lastSync = preferences.lastFoodSync.first()
            val now = currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L

            if (now - lastSync > oneDayMs) {
                runCatching { foodRepository.syncFoodsFromFirestore() }
                    .onSuccess { preferences.saveLastFoodSync(now) }
            }
        }
    }

    private fun checkLocalSession() {
        scope.launch {
            userProfileRepository.getUserProfileData().collect { profile ->
                if (profile != null) {
                    _authState.value = AuthState.Success(hasProfile = true)
                } else {
                    restoreRemoteSession()
                }
            }
        }
    }

    private fun restoreRemoteSession() {
        val user = currentUser() ?: return
        scope.launch {
            val profile = userProfileRepository.fetchUserProfileFromFirestore(user.uid)
            if (profile != null) {
                userProfileRepository.saveUserProfileData(profile)
                _authState.value = AuthState.Success(hasProfile = true)
            } else {
                _authState.value = AuthState.Success(hasProfile = false)
            }
        }
    }

    fun signInWithGoogle(signIn: suspend () -> AuthResult) {
        scope.launch {
            _authState.value = AuthState.Loading
            when (val result = signIn()) {
                is AuthResult.Success -> {
                    if (result.user.isAnonymous) {
                        _authState.value = AuthState.Success(hasProfile = false)
                        return@launch
                    }
                    val profile = userProfileRepository.fetchUserProfileFromFirestore(result.user.uid)
                    if (profile != null) {
                        userProfileRepository.saveUserProfileData(profile)
                        _authState.value = AuthState.Success(hasProfile = true)
                    } else {
                        _authState.value = AuthState.Success(hasProfile = false)
                    }
                }
                is AuthResult.Error -> _authState.value = AuthState.Error(result.message)
                AuthResult.Cancelled -> _authState.value = AuthState.Idle
            }
        }
    }

    fun clearError() {
        _authState.value = AuthState.Idle
    }
}
