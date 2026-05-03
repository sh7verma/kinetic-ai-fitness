package com.shverma.kinetic.data.repository

import android.util.Log
import com.shverma.kinetic.BuildConfig
import com.shverma.kinetic.data.model.AIResponse
import com.shverma.kinetic.data.model.MealPlan
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.network.AIPrompts
import com.shverma.kinetic.data.network.ChatType
import com.shverma.kinetic.data.network.OpenAIMessage
import com.shverma.kinetic.data.network.OpenAIRequest
import com.shverma.kinetic.data.network.OpenAIService
import com.shverma.kinetic.data.network.buildUserContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPlanner @Inject constructor(
    private val openAIService: OpenAIService,
    private val nutritionEnricher: NutritionEnricher
) {
    private val TAG = "MealPlanner"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun getMealPlanFromAI(user: UserProfileData, message: String): MealPlan {
        val request = OpenAIRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                OpenAIMessage(
                    role = "system",
                    content = AIPrompts.mealSystemPrompt2()
                ),
                OpenAIMessage(
                    role = "user",
                    content = buildUserContext(
                        chatType = ChatType.MEALS,
                        user = user,
                        message = message,
                        weeklyWorkouts = "",
                        currentWorkoutPlan = "",
                        currentMealPlan = "",
                        weeklyMeals = ""
                    )
                )
            ),
            maxCompletionTokens = 600
        )

        return try {
            val response = openAIService.getChatCompletions(
                auth = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = request
            )

            val content = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("Empty AI response")

            Log.d(TAG, "Meal Plan AI response: $content")

            val aiResponse = AIResponse.parse(content)
            if (aiResponse is AIResponse.MealPlanResult) {

                val baseMealPlan = aiResponse.data
                val enrichedMealPlan = nutritionEnricher
                    .enrichMealPlanWithNutrition(baseMealPlan)

                enrichedMealPlan
            } else {
                throw Exception("Failed to parse meal plan from AI response")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting meal plan from AI: ${e.message}", e)
            throw e
        }
    }
}
