package com.shverma.kinetic.data.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseUser

interface GoogleAuthRepository {
    suspend fun signInWithGoogle(activity: Activity): GoogleSignInResult
    suspend fun signInWithTestUser(): GoogleSignInResult
    fun getCurrentUser(): FirebaseUser?
    fun signOut()
    suspend fun deleteAccount()
}
