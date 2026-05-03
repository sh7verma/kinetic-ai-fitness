package com.shverma.kinetic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class FoodSource {
    usda,
    estimated,
    ai,
    user;

    companion object {
        fun from(value: String?): FoodSource {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: usda
        }
    }
}

@Serializable
@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val source: FoodSource = FoodSource.usda,
    val quantity: String = "",
    val unit: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
