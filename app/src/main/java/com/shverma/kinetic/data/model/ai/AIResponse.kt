package com.shverma.kinetic.data.model.ai


import android.util.Log
import com.shverma.kinetic.data.model.WorkoutPlanResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

sealed class AIResponse {

    data class WorkoutPlan(val data: WorkoutPlanResponse) : AIResponse()
    data class TargetCaloriesResult(val data: TargetCaloriesData) : AIResponse()

    data class NutritionStrategyResult(val data: NutritionStrategy) : AIResponse()
    data class DietPlanResult(val data: AIDietPlanResponse) : AIResponse()

    data class TextAnswer(val message: String) : AIResponse()

    data class Error(val message: String) : AIResponse()

    companion object {

        private const val TAG = "AIResponse"

        fun parse(text: String): AIResponse {
            Log.d(TAG, "Parsing text: $text")
            return try {
                val cleaned = text
                    .trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                Log.d(TAG, "Cleaned text: $cleaned")

                // check type field first
                val type = extractType(cleaned)
                Log.d(TAG, "Extracted type: $type")

                when (type) {
                    "workout" -> {
                        val parsed = json.decodeFromString<WorkoutPlanResponse>(cleaned)
                        Log.d(TAG, "Parsed WorkoutPlan")
                        WorkoutPlan(parsed)
                    }


                    "target_calories" -> {
                        val parsed = json.decodeFromString<TargetCaloriesResponse>(cleaned)
                        Log.d(TAG, "Parsed TargetCalories")
                        TargetCaloriesResult(parsed.data)
                    }

                    "nutrition_strategy" -> {
                        val parsed = json.decodeFromString<NutritionStrategy>(cleaned)
                        Log.d(TAG, "Parsed NutritionStrategy: $parsed")
                        NutritionStrategyResult(parsed)
                    }
                    "diet_plan" -> {
                        val parsed = json.decodeFromString<AIDietPlanResponse>(cleaned)
                        Log.d(TAG, "Parsed FoodEntityAiResponse")

                        DietPlanResult(parsed)
                    }

                    null -> {
                        Log.d(TAG, "Type is null, returning TextAnswer")
                        TextAnswer(text.trim())
                    }

                    else -> {
                        Log.d(TAG, "Unknown type $type, returning TextAnswer")
                        TextAnswer(text.trim())
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error parsing AI response: ${e.message}", e)
                TextAnswer(text.trim())
            }
        }

        private fun extractType(json: String): String? {
            // simple regex to extract "type" value without full JSON parse
            val match = Regex(""""type"\s*:\s*"([^"]+)"""").find(json)
            return match?.groupValues?.get(1)
        }
    }
}


@Serializable
data class TargetCaloriesResponse(
    val type: String = "target_calories",
    val data: TargetCaloriesData = TargetCaloriesData()
)

@Serializable
data class TargetCaloriesData(
    @SerialName("target_calories")
    val targetCalories: Double = 0.0,
    @SerialName("protein_g")
    val proteinG: Double = 0.0,
    @SerialName("carbs_g")
    val carbsG: Double = 0.0,
    @SerialName("fats_g")
    val fatsG: Double = 0.0,
    val explanation: String = ""
)


@Serializable
data class NutritionStrategy(
    val goal: String = "",

    @SerialName("protein_per_kg")
    val proteinPerKg: Double = 0.0,

    @SerialName("fat_ratio")
    val fatRatio: Double = 0.0,

    @SerialName("calorie_adjustment")
    val calorieAdjustment: Double = 0.0,

    val reasoning: String = ""
)