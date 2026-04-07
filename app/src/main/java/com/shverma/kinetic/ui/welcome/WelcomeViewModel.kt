package com.shverma.kinetic.ui.welcome

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.auth.GoogleAuthRepository
import com.shverma.kinetic.data.auth.GoogleSignInResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val authRepository: GoogleAuthRepository,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = when (val result = authRepository.signInWithGoogle(activity)) {
                is GoogleSignInResult.Success -> AuthState.Success
                is GoogleSignInResult.Error -> AuthState.Error(result.message)
                GoogleSignInResult.Cancelled -> AuthState.Idle
            }
        }
    }

    fun clearError() {
        _authState.value = AuthState.Idle
    }
}
