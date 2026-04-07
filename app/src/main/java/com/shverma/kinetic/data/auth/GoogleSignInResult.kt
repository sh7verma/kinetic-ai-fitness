package com.shverma.kinetic.data.auth

import com.google.firebase.auth.FirebaseUser

sealed class GoogleSignInResult {
    data class Success(val user: FirebaseUser) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
}
