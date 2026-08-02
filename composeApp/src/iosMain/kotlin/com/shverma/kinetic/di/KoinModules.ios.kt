package com.shverma.kinetic.di

import co.touchlab.kermit.Logger
import com.shverma.kinetic.data.auth.AuthSession
import com.shverma.kinetic.data.auth.GitLiveAuthSession
import com.shverma.kinetic.data.auth.GoogleSignInLauncher
import com.shverma.kinetic.data.auth.IosGoogleSignInLauncher
import com.shverma.kinetic.data.auth.IosUnavailableAuthSession
import com.shverma.kinetic.data.auth.IosUnavailableFoodRemoteDataSource
import com.shverma.kinetic.data.auth.IosUnavailableUserProfileRepository
import com.shverma.kinetic.data.local.AppDatabase
import com.shverma.kinetic.data.local.createAppDatabase
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.local.getDatabaseBuilder
import com.shverma.kinetic.data.network.FoodAIService
import com.shverma.kinetic.data.network.ktor.KtorOpenAIClient
import com.shverma.kinetic.data.network.ktor.createPlatformKtorOpenAIClient
import com.shverma.kinetic.data.preference.KineticDataStore
import com.shverma.kinetic.data.preference.KineticPreferences
import com.shverma.kinetic.data.preference.createKineticDataStore
import com.shverma.kinetic.data.repository.AINutritionDataSource
import com.shverma.kinetic.data.repository.FoodRemoteDataSource
import com.shverma.kinetic.data.repository.GitLiveFoodRemoteDataSource
import com.shverma.kinetic.data.repository.GitLiveUserProfileRepository
import com.shverma.kinetic.data.repository.LocalFoodDataSource
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.logging.configureKineticLogging
import com.shverma.kinetic.logging.IosKermitLogWriter
import com.shverma.kinetic.ui.fuel.FuelClock
import com.shverma.kinetic.ui.fuel.IosFuelClock
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import co.touchlab.kermit.platformLogWriter

private const val OPENAI_API_KEY = "OPENAI_API_KEY"
private var isKineticIosKoinInitialized = false

private fun isIosFirebaseConfigured(): Boolean =
    NSBundle.mainBundle.pathForResource("GoogleService-Info", ofType = "plist") != null

/** Koin graph for the iOS host; SDKs and filesystem paths stay platform-owned. */
val kineticIosModule: Module = module {
    single<KineticPreferences> {
        KineticDataStore(createKineticDataStore(preferencesFilePath()))
    }
    single<KtorOpenAIClient> { createPlatformKtorOpenAIClient() }
    single<FoodAIService> {
        FoodAIService(
            openAIClient = get(),
            apiKey = NSBundle.mainBundle.objectForInfoDictionaryKey(OPENAI_API_KEY) as? String ?: "",
        )
    }

    single<AppDatabase> { createAppDatabase(getDatabaseBuilder()) }
    single<FoodDao> { get<AppDatabase>().foodDao() }
    single<FoodLogDao> { get<AppDatabase>().foodLogDao() }

    single<FoodRemoteDataSource> {
        if (isIosFirebaseConfigured()) {
            GitLiveFoodRemoteDataSource()
        } else {
            IosUnavailableFoodRemoteDataSource()
        }
    }
    single<LocalFoodDataSource> { LocalFoodDataSource(get()) }
    single<AINutritionDataSource> { AINutritionDataSource(get()) }
    single<UserProfileRepository> {
        if (isIosFirebaseConfigured()) {
            GitLiveUserProfileRepository(
                preferences = get(),
                foodAIService = get(),
            )
        } else {
            IosUnavailableUserProfileRepository(
                preferences = get(),
                foodAIService = get(),
            )
        }
    }
    single<AuthSession> {
        if (isIosFirebaseConfigured()) GitLiveAuthSession() else IosUnavailableAuthSession()
    }
    single<GoogleSignInLauncher> {
        IosGoogleSignInLauncher(
            authSession = get(),
            firebaseConfigured = isIosFirebaseConfigured(),
        )
    }
    single<FuelClock> { IosFuelClock() }

    includes(kineticCommonBindings())
}

/** Starts the iOS Koin graph once from the Swift/UIViewController host. */
fun initializeKineticIosKoin() {
    if (!isKineticIosKoinInitialized) {
        configureKineticLogging()
        Logger.setLogWriters(listOf(IosKermitLogWriter(), platformLogWriter()))
        Logger.setTag("Kinetic")
        Logger.i("iOS Kermit logging configured")
        startKoin {
            modules(kineticIosModule)
        }
        isKineticIosKoinInitialized = true
        Logger.withTag("Kinetic").i("iOS Koin initialized")
    }
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
private fun preferencesFilePath(): String {
    val directory = requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )?.path,
    )
    return "$directory/kinetic_prefs.preferences_pb"
}
