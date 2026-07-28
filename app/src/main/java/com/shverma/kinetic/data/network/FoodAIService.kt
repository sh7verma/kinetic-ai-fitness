package com.shverma.kinetic.data.network

import android.util.Log
import com.shverma.kinetic.BuildConfig
import com.shverma.kinetic.data.model.FoodEntityAiItem
import com.shverma.kinetic.data.model.FoodEntityAiResponse
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.model.ai.AIFoodItem
import com.shverma.kinetic.data.model.ai.AILogResponse
import com.shverma.kinetic.data.model.ai.AIResponse
import com.shverma.kinetic.data.model.ai.NutritionStrategy
import com.shverma.kinetic.data.model.ai.TargetCaloriesData
import com.shverma.kinetic.data.repository.MacrosCalculator
import com.shverma.kinetic.utils.safeAiCallWithContent
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

enum class ChatType {
    MEALS,
    LOG_MEAL
}

@Singleton
class FoodAIService @Inject constructor(
    private val openAIService: OpenAIService
) {

    companion object {
        private const val TAG = "FoodAIService"
    }

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Batch nutrition fetch (PER 100g)
     */
    suspend fun getNutritionPer100g(
        items: List<AIFoodItem>
    ): List<FoodEntityAiItem> {

        if (items.isEmpty()) return emptyList()

        return try {
            val request = OpenAIRequest(
                model = "gpt-4o-mini",
                messages = listOf(
                    OpenAIMessage("system", "Return ONLY valid JSON."),
                    OpenAIMessage(
                        "user",
                        AIPrompts.getNutritionPer100g(items)
                    )
                ),
                maxCompletionTokens = 500
            )

            val validItems = safeAiCallWithContent(
                apiCall = {
                    openAIService.getChatCompletions(
                        auth = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                        request = request
                    )
                },
                transform = { cleaned ->
                    val parsed = json.decodeFromString<FoodEntityAiResponse>(cleaned)
                    parsed.items.filter { isValid(it) }
                }
            )

            if (validItems.isEmpty()) {
                Log.e(TAG, "No valid AI items, using fallback")
                return fallback(items)
            }

            validItems

        } catch (e: Exception) {
            Log.e(TAG, "AI batch failed: ${e.message}")
            fallback(items)
        }
    }


    suspend fun logFood(user: UserProfileData, message: String): AILogResponse {
        val request = OpenAIRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                OpenAIMessage(
                    role = "system",
                    content = AIPrompts.logMealSystemPrompt()
                ),
                OpenAIMessage(
                    role = "user",
                    content = buildUserContext(
                        chatType = ChatType.LOG_MEAL,
                        user = user,
                        message = message,
                        currentMealPlan = "",
                        weeklyMeals = ""
                    )
                )
            ),
            maxCompletionTokens = 600
        )

        return safeAiCallWithContent(
            apiCall = {
                openAIService.logFood(
                    auth = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                    request = request
                )
            },
            transform = { cleaned ->
                json.decodeFromString<AILogResponse>(cleaned)
            }
        )
    }

    suspend fun getInitialTargetCalories(user: UserProfileData): Pair<TargetCaloriesData, NutritionStrategy>? {
        return try {
            val request = OpenAIRequest(
                model = "gpt-4o-mini",
                messages = listOf(
                    OpenAIMessage(
                        role = "system",
                        content = AIPrompts.initStrategyPrompt()
                    ),
                    OpenAIMessage(
                        role = "user",
                        content = "User: ${user.name}, Age: ${user.age}, Weight: ${user.weight}kg, Height: ${user.height}cm, Activity: ${user.activityLevel}, Goal: ${user.workoutGoal}"
                    )
                ),
                maxCompletionTokens = 500
            )

            val response = openAIService.getChatCompletions(
                auth = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = request
            )

            val content = response.choices.firstOrNull()?.message?.content ?: return null
            val aiResponse = AIResponse.parse(content)

            if (aiResponse is AIResponse.NutritionStrategyResult) {
                val strategy = aiResponse.data
                val targets = MacrosCalculator.calculate(user, strategy)
                if (targets != null) {
                    Pair(targets, strategy)
                } else {
                    null
                }
            } else {
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "getInitialTargetCalories failed: ${e.message}")
            null
        }
    }

    private fun buildUserContext(
        chatType: ChatType,
        user: UserProfileData,
        message: String,
        currentMealPlan: String,
        weeklyMeals: String
    ): String {
        return """
            [USER_PROFILE]
            Name: ${user.name}
            Age: ${user.age}
            Weight: ${user.weight}kg
            Height: ${user.height}cm
            Activity: ${user.activityLevel}
            Goal: ${user.workoutGoal}
            
            [CHAT_TYPE]: ${chatType.name}
            [CURRENT_MEAL_PLAN]: $currentMealPlan
            [WEEKLY_HISTORY]: $weeklyMeals
            
            [USER_MESSAGE]: $message
        """.trimIndent()
    }

    private fun fallback(items: List<AIFoodItem>): List<FoodEntityAiItem> {
        return items.map {
            FoodEntityAiItem(
                name = it.food,
                caloriesPer100g = 100.0,
                proteinPer100g = 5.0,
                carbsPer100g = 15.0,
                fatsPer100g = 2.0
            )
        }
    }

    /**
     * Validation layer
     */
    private fun isValid(item: FoodEntityAiItem): Boolean {
        return item.caloriesPer100g in 10.0..900.0 &&
                item.proteinPer100g >= 0 &&
                item.carbsPer100g >= 0 &&
                item.fatsPer100g >= 0
    }

}