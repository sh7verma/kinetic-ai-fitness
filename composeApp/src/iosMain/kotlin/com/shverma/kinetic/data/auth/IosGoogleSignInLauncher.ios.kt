package com.shverma.kinetic.data.auth

import com.shverma.kinetic.ui.welcome.AuthResult
import com.shverma.kinetic.ui.welcome.AuthUser

/** Bridges the Swift-hosted Google UI into the shared GitLive authentication session. */
class IosGoogleSignInLauncher(
    private val authSession: AuthSession,
    private val firebaseConfigured: Boolean,
) : GoogleSignInLauncher {
    override suspend fun signInWithGoogle(): AuthResult = runCatching {
        if (!firebaseConfigured) {
            return AuthResult.Error(FIREBASE_CONFIGURATION_ERROR)
        }
        when (val result = requestIosGoogleIdToken()) {
            is IosGoogleSignInResult.Success ->
                authSession.signInWithGoogleIdToken(result.idToken)
                    .toAuthResult("Authentication returned no user.")

            is IosGoogleSignInResult.Error -> AuthResult.Error(result.message)
            IosGoogleSignInResult.Cancelled -> AuthResult.Cancelled
        }
    }.getOrElse { error ->
        AuthResult.Error(error.message ?: "Google sign-in failed.")
    }

    override suspend fun signInWithTestUser(): AuthResult = runCatching {
        if (!firebaseConfigured) {
            return AuthResult.Error(FIREBASE_CONFIGURATION_ERROR)
        }
        authSession.signInAnonymously().toAuthResult("Anonymous sign-in returned no user.")
    }.getOrElse { error ->
        AuthResult.Error(error.message ?: "Test user sign-in failed.")
    }
}

private fun AuthUser?.toAuthResult(errorMessage: String): AuthResult =
    this?.let(AuthResult::Success) ?: AuthResult.Error(errorMessage)

private const val FIREBASE_CONFIGURATION_ERROR =
    "Firebase iOS configuration is required before sign-in can run."
