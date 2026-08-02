package com.shverma.kinetic.data.model

import com.shverma.kinetic.data.model.ai.NutritionStrategy
import com.shverma.kinetic.data.model.ai.TargetCaloriesData
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileData(
    val uid: String = "",
    val age: Double = 28.0,
    val name: String = "User",
    val weight: Double = 75.0,
    val weightUnit: String = "KG",
    val height: Double = 175.0,
    val heightUnit: String = "CM",
    val sex: String = "MALE",
    val workoutGoal: String = "FAT LOSS",
    val commitmentDays: Double = 4.0,
    val equipment: String = "FULL GYM",
    val dietTypes: List<String> = listOf("BALANCED"),
    val allergies: List<String> = emptyList(),
    val activityLevel: String = "ACTIVE",
    val cuisines: List<String> = listOf("MEDITERRANEAN"),
    val targetCaloriesData: TargetCaloriesData? = null,
    val nutritionStrategy: NutritionStrategy? = null,
    val isCompleted: Boolean = false
)