package com.shverma.kinetic.data.auth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUUID
import kotlin.coroutines.resume

internal const val IOS_GOOGLE_SIGN_IN_REQUEST_NOTIFICATION =
    "com.shverma.kinetic.googleSignInRequest"

private const val REQUEST_ID_KEY = "requestId"

internal sealed interface IosGoogleSignInResult {
    data class Success(val idToken: String) : IosGoogleSignInResult
    data class Error(val message: String) : IosGoogleSignInResult
    data object Cancelled : IosGoogleSignInResult
}

private val pendingRequests = mutableMapOf<String, CancellableContinuation<IosGoogleSignInResult>>()

/**
 * Called by the Swift host after GIDSignIn completes. The native SDK stays in Swift because the
 * host owns the presenting UIViewController and URL callback lifecycle.
 */
@OptIn(ExperimentalForeignApi::class)
fun completeIosGoogleSignIn(
    requestId: String,
    idToken: String?,
    errorMessage: String?,
    cancelled: Boolean,
) {
    val continuation = pendingRequests.remove(requestId) ?: return
    continuation.resume(
        when {
            cancelled -> IosGoogleSignInResult.Cancelled
            !idToken.isNullOrBlank() -> IosGoogleSignInResult.Success(idToken)
            else -> IosGoogleSignInResult.Error(
                errorMessage ?: "Google Sign-In did not return an ID token.",
            )
        },
    )
}

@OptIn(ExperimentalForeignApi::class)
internal suspend fun requestIosGoogleIdToken(): IosGoogleSignInResult =
    suspendCancellableCoroutine { continuation ->
        val requestId = NSUUID().UUIDString
        pendingRequests[requestId] = continuation
        NSNotificationCenter.defaultCenter.postNotificationName(
            aName = IOS_GOOGLE_SIGN_IN_REQUEST_NOTIFICATION,
            `object` = null,
            userInfo = mapOf(REQUEST_ID_KEY to requestId),
        )
        continuation.invokeOnCancellation {
            pendingRequests.remove(requestId)
        }
    }
