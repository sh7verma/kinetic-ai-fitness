package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.model.NutritionStrategy
import com.shverma.kinetic.data.model.TargetCaloriesData
import com.shverma.kinetic.data.model.UserProfileData
import kotlin.math.round

object MacrosCalculator {
    fun calculate(profile: UserProfileData, strategy: NutritionStrategy): TargetCaloriesData? {

        val strategy = strategy

        // 1. Activity Multiplier
        val activityMultiplier = when (profile.activityLevel) {
            "SEDENTARY" -> 1.2
            "LIGHT" -> 1.375
            "MODERATE" -> 1.55
            "ACTIVE" -> 1.725
            "VERY ACTIVE" -> 1.9
            else -> 1.55
        }

        val isMale = profile.sex == "MALE"

        // 2. BMR (Mifflin-St Jeor)
        val bmr = if (isMale) {
            10 * profile.weight + 6.25 * profile.height - 5 * profile.age + 5
        } else {
            10 * profile.weight + 6.25 * profile.height - 5 * profile.age - 161
        }

        // 3. TDEE
        val tdee = bmr * activityMultiplier

        // 4. Clamp AI values (never trust raw AI)
        val proteinPerKg = strategy.proteinPerKg.coerceIn(1.6, 2.4)
        val fatRatio = strategy.fatRatio.coerceIn(0.2, 0.3)
        val calorieAdjustment = strategy.calorieAdjustment.coerceIn(-0.25, 0.15)

        // 5. Target Calories
        val targetCalories = tdee * (1 + calorieAdjustment)

        // 6. Protein
        val proteinG = profile.weight * proteinPerKg
        val proteinCalories = proteinG * 4

        // 7. Fats
        val fatCalories = targetCalories * fatRatio
        val fatsG = fatCalories / 9

        // 8. Carbs (remaining)
        val remainingCalories = targetCalories - (proteinCalories + fatCalories)
        val carbsG = remainingCalories / 4

        // 9. Final validation (force consistency)
        val finalCalories = (proteinG * 4) + (carbsG * 4) + (fatsG * 9)

        return TargetCaloriesData(
            targetCalories = finalCalories.round(),
            proteinG = proteinG.round(),
            carbsG = carbsG.round(),
            fatsG = fatsG.round(),
            explanation = strategy.reasoning
        )
    }

    // ✅ Safe fallback if AI fails
    fun fallback(profile: UserProfileData): TargetCaloriesData {
        val weight = profile.weight

        return TargetCaloriesData(
            targetCalories = 2000.0,
            proteinG = (weight * 2.0).round(),
            carbsG = 200.0,
            fatsG = 60.0,
            explanation = "Fallback default calculation"
        )
    }

}

fun Double.round(): Double {
    return round(this * 10) / 10.0
}