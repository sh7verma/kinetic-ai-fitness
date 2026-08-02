package com.shverma.kinetic.ui.welcome

data class AuthUser(
    val uid: String,
    val isAnonymous: Boolean = false,
)

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data object Cancelled : AuthResult()
}
