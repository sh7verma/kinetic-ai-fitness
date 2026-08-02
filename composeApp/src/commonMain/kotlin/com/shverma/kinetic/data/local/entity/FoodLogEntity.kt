package com.shverma.kinetic.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.shverma.kinetic.utils.currentTimeMillis

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val foodId: Int,
    val grams: Double,
    val timestamp: Long? = null,
    val mealType: String,
    val dietPlanId: Long? = null,
    val createdAt: Long = currentTimeMillis(),
)

data class FoodLogWithFood(
    @Embedded val log: FoodLogEntity,
    @Relation(
        parentColumn = "foodId",
        entityColumn = "foodId",
    )
    val food: FoodEntity,
)
