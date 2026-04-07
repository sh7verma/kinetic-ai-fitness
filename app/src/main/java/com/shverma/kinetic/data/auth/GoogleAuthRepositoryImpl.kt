package com.shverma.kinetic.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.shverma.kinetic.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context,
) : GoogleAuthRepository {

    private val credentialManager = CredentialManager.create(context)

    override suspend fun signInWithGoogle(activity: Activity): GoogleSignInResult {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                GoogleSignInResult.Success(authResult.user!!)
            } else {
                GoogleSignInResult.Error("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: Exception) {
            GoogleSignInResult.Error(e.message ?: "Sign-in failed")
        }
    }

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    override fun signOut() = firebaseAuth.signOut()
}
