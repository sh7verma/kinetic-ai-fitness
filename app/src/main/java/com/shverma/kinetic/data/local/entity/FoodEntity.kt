package com.shverma.kinetic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double
)
