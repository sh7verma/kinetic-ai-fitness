package com.shverma.kinetic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shverma.kinetic.data.model.MealItem

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val time: String,
    val date: String,
    val items: List<MealItem>,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFats: Double,
    val timestamp: Long = System.currentTimeMillis()
)
