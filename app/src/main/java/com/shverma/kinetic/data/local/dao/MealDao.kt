package com.shverma.kinetic.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shverma.kinetic.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE date = :date ORDER BY timestamp DESC")
    fun getMealsByDate(date: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE date >= :startDate ORDER BY date ASC")
    fun getMealsFromDate(startDate: String): Flow<List<MealEntity>>

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMealById(id: Long)
}
