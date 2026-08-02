package com.shverma.kinetic.data.model.ai


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

sealed class AIResponse {

    data class TargetCaloriesResult(val data: TargetCaloriesData) : AIResponse()

    data class NutritionStrategyResult(val data: NutritionStrategy) : AIResponse()

    data class TextAnswer(val message: String) : AIResponse()

    data class Error(val message: String) : AIResponse()

    companion object {

        fun parse(text: String): AIResponse {
            return try {
                val cleaned = text
                    .trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                // check type field first
                val type = extractType(cleaned)

                when (type) {
                    "target_calories" -> {
                        val parsed = json.decodeFromString<TargetCaloriesResponse>(cleaned)
                        TargetCaloriesResult(parsed.data)
                    }

                    "nutrition_strategy" -> {
                        val parsed = json.decodeFromString<NutritionStrategy>(cleaned)
                        NutritionStrategyResult(parsed)
                    }
                    null -> {
                        TextAnswer(text.trim())
                    }

                    else -> {
                        TextAnswer(text.trim())
                    }
                }

            } catch (e: Exception) {
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
