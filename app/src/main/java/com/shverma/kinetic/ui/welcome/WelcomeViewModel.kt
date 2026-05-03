package com.shverma.kinetic.ui.welcome

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.auth.GoogleAuthRepository
import com.shverma.kinetic.data.auth.GoogleSignInResult
import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.data.repository.FoodRepository
import com.shverma.kinetic.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val hasProfile: Boolean) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val authRepository: GoogleAuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val foodRepository: FoodRepository,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkLocalSession()
        syncFoodsIfNeeded()
    }

    private fun syncFoodsIfNeeded() {
        viewModelScope.launch {
            val lastSync = dataStoreHelper.lastFoodSync.first()
            val currentTime = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L

            if (currentTime - lastSync > oneDayMs) {
                try {
                    foodRepository.syncFoodsFromFirestore()
                    dataStoreHelper.saveLastFoodSync(currentTime)
                } catch (e: Exception) {
                    android.util.Log.e("WelcomeViewModel", "Food sync failed", e)
                }
            }
        }
    }

    private fun checkLocalSession() {
        viewModelScope.launch {
            userProfileRepository.getUserProfileData().collect { profile ->
                if (profile != null) {
                    _authState.value = AuthState.Success(hasProfile = true)
                } else {
                    checkFirebaseUser()
                }
            }
        }
    }

    private fun checkFirebaseUser() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                val profile = userProfileRepository.fetchUserProfileFromFirestore(currentUser.uid)
                if (profile != null) {
                    userProfileRepository.saveUserProfileData(profile)
                    _authState.value = AuthState.Success(hasProfile = true)
                } else {
                    _authState.value = AuthState.Success(hasProfile = false)
                }
            }
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = authRepository.signInWithGoogle(activity)) {
                is GoogleSignInResult.Success -> {
                    val user = result.user
                    // Check if it's a test user (anonymous)
                    if (user.isAnonymous) {
                        // For test users, we might want to skip Firestore check and go to Success(hasProfile = false)
                        // Or if they have a local profile already, use that.
                        _authState.value = AuthState.Success(hasProfile = false)
                        return@launch
                    }
                    val profile = userProfileRepository.fetchUserProfileFromFirestore(user.uid)
                    if (profile != null) {
                        userProfileRepository.saveUserProfileData(profile)
                        _authState.value = AuthState.Success(hasProfile = true)
                    } else {
                        _authState.value = AuthState.Success(hasProfile = false)
                    }
                }
                is GoogleSignInResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                GoogleSignInResult.Cancelled -> {
                    _authState.value = AuthState.Idle
                }
            }
        }
    }

    fun clearError() {
        _authState.value = AuthState.Idle
    }
}
