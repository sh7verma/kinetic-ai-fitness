package com.shverma.kinetic.di

import com.shverma.kinetic.BuildConfig
import com.shverma.kinetic.data.auth.AuthSession
import com.shverma.kinetic.data.auth.AndroidActivityProvider
import com.shverma.kinetic.data.auth.AndroidGoogleSignInLauncher
import com.shverma.kinetic.data.auth.GitLiveAuthSession
import com.shverma.kinetic.data.auth.GoogleAuthRepository
import com.shverma.kinetic.data.auth.GoogleAuthRepositoryImpl
import com.shverma.kinetic.data.auth.GoogleSignInLauncher
import com.shverma.kinetic.data.local.AppDatabase
import com.shverma.kinetic.data.local.createAppDatabase
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.local.getDatabaseBuilder
import com.shverma.kinetic.data.network.FoodAIService
import com.shverma.kinetic.data.network.ktor.KtorOpenAIClient
import com.shverma.kinetic.data.network.ktor.createPlatformKtorOpenAIClient
import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.data.preference.KineticPreferences
import com.shverma.kinetic.data.repository.AINutritionDataSource
import com.shverma.kinetic.data.repository.FoodRemoteDataSource
import com.shverma.kinetic.data.repository.GitLiveFoodRemoteDataSource
import com.shverma.kinetic.data.repository.GitLiveUserProfileRepository
import com.shverma.kinetic.data.repository.LocalFoodDataSource
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.ui.aichat.AIChatViewModel
import com.shverma.kinetic.ui.fuel.FuelViewModel
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel
import com.shverma.kinetic.ui.profile.ProfileViewModel
import com.shverma.kinetic.ui.welcome.WelcomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val kineticAndroidModule: Module = module {
    single { DataStoreHelper(androidContext()) }
    single<KineticPreferences> { get<DataStoreHelper>() }
    single<KtorOpenAIClient> { createPlatformKtorOpenAIClient() }
    single {
        FoodAIService(
            openAIClient = get(),
            apiKey = BuildConfig.OPENAI_API_KEY,
        )
    }

    single<AppDatabase> { createAppDatabase(getDatabaseBuilder(androidContext())) }
    single<FoodDao> { get<AppDatabase>().foodDao() }
    single<FoodLogDao> { get<AppDatabase>().foodLogDao() }

    single { GitLiveFoodRemoteDataSource() }
    single<FoodRemoteDataSource> { get<GitLiveFoodRemoteDataSource>() }
    single { LocalFoodDataSource(get()) }
    single { AINutritionDataSource(get()) }

    single<UserProfileRepository> {
        GitLiveUserProfileRepository(
            preferences = get(),
            foodAIService = get(),
        )
    }
    single<GoogleAuthRepository> {
        GoogleAuthRepositoryImpl(
            context = androidContext(),
        )
    }
    single { AndroidActivityProvider() }
    single<GoogleSignInLauncher> {
        AndroidGoogleSignInLauncher(
            repository = get(),
            activityProvider = get(),
            authSession = get(),
        )
    }
    single<AuthSession> { GitLiveAuthSession() }

    includes(kineticCommonBindings())

    viewModel { WelcomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { OnboardingViewModel(get(), get()) }
    viewModel { AIChatViewModel(get(), get(), get(), get()) }
    viewModel { FuelViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
}
