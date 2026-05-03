package com.shverma.kinetic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shverma.kinetic.data.local.dao.ExerciseDao
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.MealDao
import com.shverma.kinetic.data.local.entity.ExerciseEntity
import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.local.entity.MealEntity

@Database(entities = [MealEntity::class, ExerciseEntity::class, FoodEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun foodDao(): FoodDao

    companion object {
        const val DATABASE_NAME = "kinetic_db"
    }
}
