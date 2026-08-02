package com.shverma.kinetic.data.auth

import com.shverma.kinetic.BuildConfig
import com.shverma.kinetic.ui.welcome.AuthResult
import com.shverma.kinetic.ui.welcome.AuthUser

/** Adapts Android Credential Manager sign-in to the common launcher contract. */
class AndroidGoogleSignInLauncher(
    private val repository: GoogleAuthRepository,
    private val activityProvider: AndroidActivityProvider,
    private val authSession: AuthSession,
) : GoogleSignInLauncher {
    override suspend fun signInWithGoogle(): AuthResult {
        val activity = activityProvider.currentActivity
            ?: return AuthResult.Error("Sign-in is unavailable because the app is not active.")
        return when (val result = repository.signInWithGoogle(activity)) {
            is GoogleSignInResult.Success -> signInWithGoogleIdToken(result.idToken)
            is GoogleSignInResult.Error -> {
                if (BuildConfig.IS_DEBUG) signInWithTestUser() else AuthResult.Error(result.message)
            }
            GoogleSignInResult.Cancelled -> AuthResult.Cancelled
        }
    }

    override suspend fun signInWithTestUser(): AuthResult = runCatching {
        authSession.signInAnonymously().toAuthResult("Anonymous sign-in returned no user.")
    }.getOrElse { error ->
        AuthResult.Error(error.message ?: "Test user sign-in failed.")
    }

    private suspend fun signInWithGoogleIdToken(idToken: String): AuthResult = runCatching {
        authSession.signInWithGoogleIdToken(idToken).toAuthResult("Authentication returned no user.")
    }.getOrElse { error ->
        AuthResult.Error(error.message ?: "Google sign-in failed.")
    }
}

private fun AuthUser?.toAuthResult(errorMessage: String): AuthResult =
    this?.let(AuthResult::Success) ?: AuthResult.Error(errorMessage)
