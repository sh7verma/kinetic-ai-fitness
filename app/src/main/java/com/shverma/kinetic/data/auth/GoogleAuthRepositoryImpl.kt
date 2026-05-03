package com.shverma.kinetic.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.shverma.kinetic.BuildConfig
import com.shverma.kinetic.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context,
) : GoogleAuthRepository {

    private val TAG = "GoogleAuthRepo"
    private val credentialManager = CredentialManager.create(context)

    override suspend fun signInWithGoogle(activity: Activity): GoogleSignInResult {
        return try {

            // ✅ ONLY use GetGoogleIdOption (do NOT mix APIs)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            Log.d(TAG, "Credential type: ${credential.type}")

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {

                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                val idToken = googleIdTokenCredential.idToken

                if (idToken.isNullOrEmpty()) {
                    if (BuildConfig.IS_DEBUG) return signInWithTestUser()
                    return GoogleSignInResult.Error("ID Token is null or empty")
                }

                val firebaseCredential =
                    GoogleAuthProvider.getCredential(idToken, null)

                val authResult =
                    firebaseAuth.signInWithCredential(firebaseCredential).await()

                val user = authResult.user

                if (user != null) {
                    Log.d(TAG, "Sign-in success: ${user.email}")
                    GoogleSignInResult.Success(user)
                } else {
                    Log.e(TAG, "Firebase user is null")
                    if (BuildConfig.IS_DEBUG) return signInWithTestUser()
                    GoogleSignInResult.Error("Authentication failed (user null)")
                }

            } else {
                Log.e(TAG, "Invalid credential type: ${credential::class.java.simpleName}")
                if (BuildConfig.IS_DEBUG) return signInWithTestUser()
                GoogleSignInResult.Error(
                    "Unexpected credential type: ${credential::class.java.simpleName}"
                )
            }

        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled sign-in")
            GoogleSignInResult.Cancelled

        } catch (e: Exception) {
            Log.e(TAG, "Sign-in error", e)

            if (BuildConfig.IS_DEBUG) {
                Log.d(TAG, "Debug mode: Falling back to test user login")
                return signInWithTestUser()
            }

            val errorMessage = when {
                e.message?.contains("No credentials", true) == true ->
                    "No Google accounts found on this device"

                e.message?.contains("network", true) == true ->
                    "Network error. Check your connection"

                else -> e.localizedMessage ?: "Google sign-in failed"
            }

            GoogleSignInResult.Error(errorMessage)
        }
    }

    override suspend fun signInWithTestUser(): GoogleSignInResult {
        return try {
            Log.d(TAG, "Signing in with test user (anonymous)")
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user
            if (user != null) {
                GoogleSignInResult.Success(user)
            } else {
                GoogleSignInResult.Error("Failed to sign in with test user")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Test user sign-in error", e)
            GoogleSignInResult.Error("Test user sign-in failed: ${e.message}")
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun deleteAccount() {
        firebaseAuth.currentUser?.delete()?.await()
    }
}