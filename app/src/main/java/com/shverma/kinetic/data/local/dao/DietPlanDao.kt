package com.shverma.kinetic.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shverma.kinetic.data.local.entity.DietPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DietPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietPlan(dietPlan: DietPlanEntity)

    @Query("SELECT * FROM diet_plans ORDER BY createdAt DESC")
    fun getAllDietPlans(): Flow<List<DietPlanEntity>>

    @Query("SELECT * FROM diet_plans WHERE id = :id")
    suspend fun getDietPlanById(id: Long): DietPlanEntity?

    @Delete
    suspend fun deleteDietPlan(dietPlan: DietPlanEntity)

    @Query("DELETE FROM diet_plans WHERE id = :id")
    suspend fun deleteDietPlanById(id: Long)
}
