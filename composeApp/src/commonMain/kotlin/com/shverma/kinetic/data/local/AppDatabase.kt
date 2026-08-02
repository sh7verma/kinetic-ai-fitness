package com.shverma.kinetic.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabaseConstructor
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.local.entity.FoodLogEntity

@Database(
    entities = [FoodEntity::class, FoodLogEntity::class],
    version = 10,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun foodLogDao(): FoodLogDao

    companion object {
        const val DATABASE_NAME = "kinetic_db"
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
