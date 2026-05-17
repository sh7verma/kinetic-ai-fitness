package com.shverma.kinetic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shverma.kinetic.data.local.entity.FoodLogEntity
import com.shverma.kinetic.data.local.entity.FoodLogWithFood
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodLog: FoodLogEntity)

    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllFoodLogs(): Flow<List<FoodLogEntity>>

    @Transaction
    @Query("SELECT * FROM food_logs WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getFoodLogsWithFoodInRange(start: Long, end: Long): Flow<List<FoodLogWithFood>>

    @Query("SELECT * FROM food_logs WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getFoodLogsInRange(start: Long, end: Long): Flow<List<FoodLogEntity>>

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
