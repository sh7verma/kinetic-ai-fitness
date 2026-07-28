package com.shverma.kinetic.di

import android.content.Context
import androidx.room.Room
import com.shverma.kinetic.data.local.AppDatabase
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFoodDao(database: AppDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    @Singleton
    fun provideFoodLogDao(database: AppDatabase): FoodLogDao {
        return database.foodLogDao()
    }
}
