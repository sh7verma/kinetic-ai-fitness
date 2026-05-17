package com.shverma.kinetic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shverma.kinetic.data.local.dao.DietPlanDao
import com.shverma.kinetic.data.local.dao.ExerciseDao
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.local.entity.DietPlanEntity
import com.shverma.kinetic.data.local.entity.ExerciseEntity
import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.local.entity.FoodLogEntity

@Database(
    entities = [ExerciseEntity::class, FoodEntity::class, DietPlanEntity::class, FoodLogEntity::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun foodDao(): FoodDao
    abstract fun dietPlanDao(): DietPlanDao
    abstract fun foodLogDao(): FoodLogDao

    companion object {
        const val DATABASE_NAME = "kinetic_db"
    }
}
