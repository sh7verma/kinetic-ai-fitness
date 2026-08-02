package com.shverma.kinetic.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.auth.AuthSession
import com.shverma.kinetic.data.auth.GoogleSignInLauncher
import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.data.repository.FoodRepository
import com.shverma.kinetic.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.StateFlow

/** Android Credential Manager/Firebase adapter for the shared Welcome controller. */
class WelcomeViewModel(
    private val googleSignInLauncher: GoogleSignInLauncher,
    authSession: AuthSession,
    userProfileRepository: UserProfileRepository,
    foodRepository: FoodRepository,
    dataStoreHelper: DataStoreHelper,
) : ViewModel() {
    private val controller = WelcomeController(
        userProfileRepository = userProfileRepository,
        foodRepository = foodRepository,
        preferences = dataStoreHelper,
        currentUser = authSession::currentUser,
        scope = viewModelScope,
    )

    val authState: StateFlow<AuthState> = controller.authState

    fun signInWithGoogle() = controller.signInWithGoogle(googleSignInLauncher::signInWithGoogle)

    fun clearError() = controller.clearError()
}
