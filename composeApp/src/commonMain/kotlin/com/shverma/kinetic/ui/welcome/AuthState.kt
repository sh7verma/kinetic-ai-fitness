package com.shverma.kinetic.ui.welcome

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val hasProfile: Boolean) : AuthState()
    data class Error(val message: String) : AuthState()
}
