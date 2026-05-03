package com.shverma.kinetic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shverma.kinetic.data.local.dao.ExerciseDao
import com.shverma.kinetic.data.local.dao.MealDao
import com.shverma.kinetic.data.local.entity.ExerciseEntity
import com.shverma.kinetic.data.local.entity.MealEntity

@Database(entities = [MealEntity::class, ExerciseEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        const val DATABASE_NAME = "kinetic_db"
    }
}
