package com.shverma.kinetic.data.auth

import com.shverma.kinetic.ui.welcome.AuthResult

/** Platform launcher boundary for Google credentials; no UI-context type leaks into common code. */
interface GoogleSignInLauncher {
    suspend fun signInWithGoogle(): AuthResult
    suspend fun signInWithTestUser(): AuthResult
}
