package com.shverma.kinetic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shverma.kinetic.utils.currentTimeMillis
import kotlinx.serialization.Serializable

@Serializable
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
    @PrimaryKey(autoGenerate = true)
    val foodId: Int = 0,

    val name: String,

    // ALWAYS per 100g
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatsPer100g: Double,

    val source: FoodSource = FoodSource.usda,

    val updatedAt: Long = currentTimeMillis()
)

data class Macros(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double
)

fun FoodLogEntity.toMacros(food: FoodEntity): Macros {
    val factor = grams / 100.0

    return Macros(
        calories = food.caloriesPer100g * factor,
        protein = food.proteinPer100g * factor,
        carbs = food.carbsPer100g * factor,
        fats = food.fatsPer100g * factor
    )
}
