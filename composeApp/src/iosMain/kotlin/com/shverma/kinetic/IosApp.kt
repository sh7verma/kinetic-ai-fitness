package com.shverma.kinetic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import com.shverma.kinetic.data.auth.AuthSession
import com.shverma.kinetic.data.auth.GoogleSignInLauncher
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.preference.KineticPreferences
import com.shverma.kinetic.data.repository.DietAIRepository
import com.shverma.kinetic.data.repository.FoodRepository
import com.shverma.kinetic.data.repository.FoodResolver
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.ui.aichat.AIChatContent
import com.shverma.kinetic.ui.aichat.AIChatController
import com.shverma.kinetic.ui.fuel.FuelContent
import com.shverma.kinetic.ui.fuel.FuelController
import com.shverma.kinetic.ui.navigation.KineticApp
import com.shverma.kinetic.ui.navigation.KineticLandingContent
import com.shverma.kinetic.ui.onboarding.OnboardingController
import com.shverma.kinetic.ui.onboarding.screens.OnboardingContent
import com.shverma.kinetic.ui.profile.ProfileContent
import com.shverma.kinetic.ui.profile.ProfileController
import com.shverma.kinetic.ui.welcome.WelcomeContent
import com.shverma.kinetic.ui.welcome.WelcomeController
import org.koin.compose.koinInject

/** iOS entry composable backed by the same feature controllers used by Android. */
@Composable
fun IosApp() {
    val scope = rememberCoroutineScope()
    val preferences = koinInject<KineticPreferences>()
    val foodRepository = koinInject<FoodRepository>()
    val userProfileRepository = koinInject<UserProfileRepository>()
    val foodResolver = koinInject<FoodResolver>()
    val dietAIRepository = koinInject<DietAIRepository>()
    val foodDao = koinInject<FoodDao>()
    val foodLogDao = koinInject<FoodLogDao>()
    val authSession = koinInject<AuthSession>()
    val googleSignInLauncher = koinInject<GoogleSignInLauncher>()
    val fuelClock = koinInject<com.shverma.kinetic.ui.fuel.FuelClock>()

    val welcomeController = remember(preferences, foodRepository, userProfileRepository, authSession, scope) {
        WelcomeController(
            userProfileRepository = userProfileRepository,
            foodRepository = foodRepository,
            preferences = preferences,
            currentUser = authSession::currentUser,
            scope = scope,
        )
    }
    val onboardingController = remember(userProfileRepository, authSession, scope) {
        OnboardingController(
            userProfileRepository = userProfileRepository,
            currentUserId = { authSession.currentUser()?.uid.orEmpty() },
            scope = scope,
        )
    }
    val aiChatController = remember(dietAIRepository, foodResolver, foodLogDao, foodDao, scope) {
        AIChatController(
            chatRepository = dietAIRepository,
            foodResolver = foodResolver,
            foodLogDao = foodLogDao,
            foodDao = foodDao,
            scope = scope,
        )
    }
    val fuelController = remember(userProfileRepository, foodLogDao, fuelClock, scope) {
        FuelController(
            userProfileRepository = userProfileRepository,
            foodLogDao = foodLogDao,
            clock = fuelClock,
            scope = scope,
        )
    }
    val profileController = remember(userProfileRepository, authSession, scope) {
        ProfileController(
            userProfileRepository = userProfileRepository,
            currentUserId = { authSession.currentUser()?.uid },
            signOut = authSession::signOut,
            deleteAuthAccount = authSession::deleteAccount,
            scope = scope,
        )
    }

    val authState by welcomeController.authState.collectAsState()

    KineticApp(
        welcomeContent = { snackbarHostState, onNavigateToOnboarding, onNavigateToLanding ->
            WelcomeContent(
                authState = authState,
                snackBarHostState = snackbarHostState,
                onSignInWithGoogle = {
                    welcomeController.signInWithGoogle {
                        googleSignInLauncher.signInWithGoogle()
                    }
                },
                onClearError = welcomeController::clearError,
                onNavigateToOnboarding = onNavigateToOnboarding,
                onNavigateToLanding = onNavigateToLanding,
            )
        },
        onboardingContent = { onFinish ->
            OnboardingContent(
                actions = onboardingController,
                onFinish = onFinish,
            )
        },
        landingContent = { onLogout ->
            KineticLandingContent(
                fuelContent = { onAIChatClick ->
                    FuelContent(
                        actions = fuelController,
                        onAIChatClick = onAIChatClick,
                    )
                },
                chatContent = { AIChatContent(actions = aiChatController) },
                profileContent = { ProfileContent(actions = profileController, onLogout = onLogout) },
                onLogout = onLogout,
            )
        },
    )
}
