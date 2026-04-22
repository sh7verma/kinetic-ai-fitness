package com.shverma.kinetic.di

import android.content.Context
import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.data.repository.HomeRepository
import com.shverma.kinetic.data.repository.HomeRepositoryImpl
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.data.repository.UserProfileRepositoryImpl
import com.shverma.kinetic.data.repository.ChatRepository
import com.shverma.kinetic.data.repository.ChatRepositoryImpl
import com.shverma.kinetic.data.repository.MealRepository
import com.shverma.kinetic.data.repository.MealRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton



@InstallIn(SingletonComponent::class)
@Module
object DispatchersModule {
    @Provides
    fun provideIODispatcher() = Dispatchers.IO
}


@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDataStoreHelper(@ApplicationContext context: Context) = DataStoreHelper(context)
}


@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ): HomeRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindMealRepository(
        mealRepositoryImpl: MealRepositoryImpl
    ): MealRepository
}