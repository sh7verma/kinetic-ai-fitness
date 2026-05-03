package com.shverma.kinetic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shverma.kinetic.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises WHERE date = :date ORDER BY timestamp DESC")
    fun getExercisesByDate(date: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises ORDER BY timestamp DESC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExerciseById(id: Long)
}
