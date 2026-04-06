package com.shverma.app.di

import android.content.Context
import com.shverma.app.data.preference.DataStoreHelper
import com.shverma.app.data.repository.HomeRepository
import com.shverma.app.data.repository.HomeRepositoryImpl
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
}