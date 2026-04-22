package com.shverma.kinetic.data.model


import android.util.Log
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

sealed class AIResponse {

    data class WorkoutPlan(val data: WorkoutPlanResponse) : AIResponse()

    data class MealPlanResult(val data: MealPlan) : AIResponse()

    data class SingleLogResult(val data: SingleLogResponse) : AIResponse()

    data class MultiLogResult(val data: MultiLogResponse) : AIResponse()

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

                    "meal_plan" -> {
                        val parsed = json.decodeFromString<MealPlanResponse>(cleaned)
                        Log.d(TAG, "Parsed MealPlan")
                        MealPlanResult(parsed.mealPlan)
                    }

                    "single_log" -> {
                        val parsed = json.decodeFromString<SingleLogResponse>(cleaned)
                        Log.d(TAG, "Parsed SingleLog")
                        SingleLogResult(parsed)
                    }

                    "multi_log" -> {
                        val parsed = json.decodeFromString<MultiLogResponse>(cleaned)
                        Log.d(TAG, "Parsed MultiLog")
                        MultiLogResult(parsed)
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