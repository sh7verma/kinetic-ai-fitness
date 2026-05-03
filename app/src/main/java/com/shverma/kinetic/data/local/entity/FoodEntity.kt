package com.shverma.kinetic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val source: String = "usda", // usda | firestore | ai
    val updatedAt: Long = System.currentTimeMillis()
)
