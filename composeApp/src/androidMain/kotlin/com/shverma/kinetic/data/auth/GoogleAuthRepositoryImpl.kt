package com.shverma.kinetic.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import co.touchlab.kermit.Logger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.shverma.kinetic.R

class GoogleAuthRepositoryImpl(
    private val context: Context,
) : GoogleAuthRepository {

    private val log = Logger.withTag("GoogleAuthRepo")
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

            log.d { "Credential type: ${credential.type}" }

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {

                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                val idToken = googleIdTokenCredential.idToken

                if (idToken.isNullOrEmpty()) {
                    return GoogleSignInResult.Error("ID Token is null or empty")
                }

                GoogleSignInResult.Success(idToken)

            } else {
                log.e { "Invalid credential type: ${credential::class.java.simpleName}" }
                GoogleSignInResult.Error(
                    "Unexpected credential type: ${credential::class.java.simpleName}"
                )
            }

        } catch (e: GetCredentialCancellationException) {
            log.d { "User cancelled sign-in" }
            GoogleSignInResult.Cancelled

        } catch (e: Exception) {
            log.e(e) { "Sign-in error" }

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
}
