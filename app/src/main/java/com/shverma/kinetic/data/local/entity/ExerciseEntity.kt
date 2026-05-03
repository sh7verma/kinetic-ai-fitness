package com.shverma.kinetic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sets: Int,
    val reps: String,
    val restSecs: Int,
    val equipment: String,
    val cue: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)
