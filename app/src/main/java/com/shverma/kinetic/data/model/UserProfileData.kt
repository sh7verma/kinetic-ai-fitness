package com.shverma.kinetic.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileData(
    val uid: String = "",
    val age: Double = 28.0,
    val name: String ="User",
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
) {
    fun calculateTargetCalories(): Double {
        val bmr = if (sex == "MALE") {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }

        val activityMultiplier = when (activityLevel) {
            "SEDENTARY" -> 1.2
            "LIGHT" -> 1.375
            "MODERATE" -> 1.55
            "ACTIVE" -> 1.725
            "VERY ACTIVE" -> 1.9
            else -> 1.5
        }

        var tdee = bmr * activityMultiplier
        return tdee.round()
    }
}

fun Double.round(): Double {
    return kotlin.math.round(this * 10) / 10.0
}
