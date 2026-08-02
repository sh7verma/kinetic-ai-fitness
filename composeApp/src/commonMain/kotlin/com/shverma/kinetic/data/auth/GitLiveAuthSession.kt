package com.shverma.kinetic.data.auth

import com.shverma.kinetic.ui.welcome.AuthUser
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth

/** Shared Firebase Auth session backed by the GitLive multiplatform SDK. */
class GitLiveAuthSession(
    private val firebaseAuth: FirebaseAuth = Firebase.auth,
) : AuthSession {
    override fun currentUser(): AuthUser? = firebaseAuth.currentUser?.let { user ->
        AuthUser(uid = user.uid, isAnonymous = user.isAnonymous)
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthUser? =
        firebaseAuth.signInWithCredential(
            GoogleAuthProvider.credential(idToken = idToken, accessToken = null),
        ).user?.toAuthUser()

    override suspend fun signInAnonymously(): AuthUser? =
        firebaseAuth.signInAnonymously().user?.toAuthUser()

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun deleteAccount() {
        firebaseAuth.currentUser?.delete()
    }

    private fun dev.gitlive.firebase.auth.FirebaseUser.toAuthUser(): AuthUser =
        AuthUser(uid = uid, isAnonymous = isAnonymous)
}
