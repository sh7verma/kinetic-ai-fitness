package com.shverma.kinetic.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
enum class DietaryGoal(
    val label: String,
    val description: String
) {
    MUSCLE_GAIN(
        label = "MUSCLE GAIN",
        description = "Caloric surplus with high-protein macro split to drive hypertrophic adaptation."
    ),
    FAT_LOSS(
        label = "FAT LOSS",
        description = "Precision deficit strategy preserving lean tissue while accelerating metabolic burn."
    );

    val icon: ImageVector
        get() = when (this) {
            MUSCLE_GAIN -> Icons.Default.Bolt
            FAT_LOSS -> Icons.Default.LocalFireDepartment
        }
}

data class DietChip(val key: String, val label: String)

val dietChips = listOf(
    DietChip("BALANCED", "BALANCED"),
    DietChip("VEGAN", "VEGAN"),
    DietChip("VEGETARIAN", "VEGETARIAN"),
    DietChip("KETO", "KETO"),
    DietChip("PALEO", "PALEO"),
    DietChip("PESCATARIAN", "PESCATARIAN")
)

data class AllergyItem(val name: String, val isCommon: Boolean)

val allergyDatabase = listOf(
    AllergyItem("Peanuts", true),
    AllergyItem("Shellfish", true),
    AllergyItem("Dairy / Lactose", true),
    AllergyItem("Gluten / Wheat", true),
    AllergyItem("Tree Nuts", true),
    AllergyItem("Eggs", false),
    AllergyItem("Soy", false),
    AllergyItem("Fish", false),
    AllergyItem("Sesame", false),
    AllergyItem("Sulphites", false)
)

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
    val dietaryGoal: DietaryGoal = DietaryGoal.MUSCLE_GAIN,
    val dietTypes: Set<String> = setOf("BALANCED"),
    val allergies: Set<String> = emptySet(),
    val activityLevel: String = "ACTIVE",
    val cuisines: Set<String> = setOf("MEDITERRANEAN"),
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
        tdee += when (dietaryGoal) {
            DietaryGoal.MUSCLE_GAIN -> 300.0
            DietaryGoal.FAT_LOSS -> -500.0
        }
        return tdee
    }
}
