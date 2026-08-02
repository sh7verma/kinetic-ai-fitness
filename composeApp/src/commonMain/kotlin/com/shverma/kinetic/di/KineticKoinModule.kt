package com.shverma.kinetic.di

import com.shverma.kinetic.data.network.FoodAIService
import com.shverma.kinetic.data.preference.KineticPreferences
import com.shverma.kinetic.data.repository.AINutritionDataSource
import com.shverma.kinetic.data.repository.DietAIRepository
import com.shverma.kinetic.data.repository.DietAIRepositoryImpl
import com.shverma.kinetic.data.repository.FoodRemoteDataSource
import com.shverma.kinetic.data.repository.FoodRepository
import com.shverma.kinetic.data.repository.FoodResolver
import com.shverma.kinetic.data.repository.LocalFoodDataSource
import com.shverma.kinetic.data.repository.UserProfileRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Platform-created dependencies needed by the shared application graph.
 *
 * Platform code owns SDK setup, secrets, file paths, and database construction;
 * the common graph owns the relationships between portable services and repositories.
 */
data class KineticPlatformDependencies(
    val preferences: KineticPreferences,
    val foodAIService: FoodAIService,
    val localFoodDataSource: LocalFoodDataSource,
    val remoteFoodDataSource: FoodRemoteDataSource,
    val aiNutritionDataSource: AINutritionDataSource,
    val userProfileRepository: UserProfileRepository,
)

fun KineticPlatformDependencies.kineticCommonModule(): Module = module {
    single<KineticPreferences> { preferences }
    single<FoodAIService> { foodAIService }
    single<LocalFoodDataSource> { localFoodDataSource }
    single<FoodRemoteDataSource> { remoteFoodDataSource }
    single<AINutritionDataSource> { aiNutritionDataSource }
    single<UserProfileRepository> { userProfileRepository }

    single { FoodResolver(get(), get(), get()) }
    single { FoodRepository(get(), get(), get()) }
    single<DietAIRepository> { DietAIRepositoryImpl(get(), get()) }
}

/** Registers relationships whose concrete instances are supplied by a platform module. */
fun kineticCommonBindings(): Module = module {
    single { FoodResolver(get(), get(), get()) }
    single { FoodRepository(get(), get(), get()) }
    single<DietAIRepository> { DietAIRepositoryImpl(get(), get()) }
}
