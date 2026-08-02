package com.shverma.kinetic.data.auth

import com.shverma.kinetic.ui.welcome.AuthUser

/** Platform-neutral authenticated-session operations used by shared controllers. */
interface AuthSession {
    fun currentUser(): AuthUser?

    suspend fun signInWithGoogleIdToken(idToken: String): AuthUser?

    suspend fun signInAnonymously(): AuthUser?

    suspend fun signOut()

    suspend fun deleteAccount()
}
