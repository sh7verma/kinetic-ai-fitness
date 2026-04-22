package com.shverma.kinetic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shverma.kinetic.data.local.dao.MealDao
import com.shverma.kinetic.data.local.entity.MealEntity

@Database(entities = [MealEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao

    companion object {
        const val DATABASE_NAME = "kinetic_db"
    }
}
