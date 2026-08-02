package com.shverma.kinetic.data.auth

import android.app.Activity

interface GoogleAuthRepository {
    suspend fun signInWithGoogle(activity: Activity): GoogleSignInResult
}
