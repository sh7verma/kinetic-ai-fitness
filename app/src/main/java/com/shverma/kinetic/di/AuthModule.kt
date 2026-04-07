package com.shverma.kinetic.di

import com.google.firebase.auth.FirebaseAuth
import com.shverma.kinetic.data.auth.GoogleAuthRepository
import com.shverma.kinetic.data.auth.GoogleAuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindGoogleAuthRepository(
        impl: GoogleAuthRepositoryImpl,
    ): GoogleAuthRepository
}
