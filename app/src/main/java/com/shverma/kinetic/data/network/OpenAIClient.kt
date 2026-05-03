package com.shverma.kinetic.data.network

import android.util.Log
import com.shverma.kinetic.BuildConfig
import com.shverma.kinetic.data.model.AIResponse
import com.shverma.kinetic.data.model.NutritionStrategy
import com.shverma.kinetic.data.model.TargetCaloriesData
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.repository.MacrosCalculator
import com.shverma.kinetic.utils.toTimeString
import java.util.Date
import kotlin.math.roundToInt
import javax.inject.Inject
import javax.inject.Singleton

enum class ChatType {
    WORKOUT, MEALS, LOG_MEAL, LOG_WORKOUT
}

@Singleton
class OpenAIClient @Inject constructor(
    private val openAIService: OpenAIService
) {

    companion object {
        private const val TAG = "OpenAIClient"
    }

    suspend fun chatWithUsage(
        user: UserProfileData,
        message: String,
        chatType: ChatType,
        weeklyWorkouts: String = "",
        currentWorkoutPlan: String = "",
        currentMealPlan: String = "",
        weeklyMeals: String = ""
    ): Pair<String?, OpenAIUsage?> {

        val systemPrompt = when (chatType) {
            ChatType.WORKOUT -> AIPrompts.workoutSystemPrompt()
            ChatType.MEALS -> AIPrompts.mealSystemPrompt()
            ChatType.LOG_MEAL -> AIPrompts.logMealSystemPrompt()
            ChatType.LOG_WORKOUT -> AIPrompts.logWorkoutSystemPrompt()
        }

        val userContext = buildUserContext(
            chatType,
            user,
            message,
            weeklyWorkouts,
            currentWorkoutPlan,
            currentMealPlan,
            weeklyMeals
        )

        Log.d(TAG, "Full Prompt:\nSystem:\n$systemPrompt\nUser:\n$userContext")

        val model = "gpt-4o-mini"

        val request = OpenAIRequest(
            model = model,
            maxCompletionTokens = when (chatType) {
                ChatType.LOG_MEAL, ChatType.LOG_WORKOUT -> 200 // small JSON
                else -> 600
            },
            messages = listOf(
                OpenAIMessage(
                    role = "system",
                    content = systemPrompt
                ),
                OpenAIMessage(
                    role = "user",
                    content = userContext
                )
            )
        )

        return try {
            val response = openAIService.getChatCompletions(
                auth = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = request
            )
            response.choices.firstOrNull()?.message?.content to response.usage
        } catch (e: Exception) {
            Log.e(TAG, "Error in OpenAI call: ${e.message}", e)
            null to null
        }
    }

    suspend fun getInitialTargetCalories(user: UserProfileData): Pair<TargetCaloriesData, NutritionStrategy>? {
        val strategy = getStrategy(user)
        return if (strategy != null) {
            val targetCalories = MacrosCalculator.calculate(user, strategy)
            if (targetCalories != null) {
                targetCalories to strategy
            } else {
                null
            }
        } else {
            Log.w(TAG, "AI failed to provide a strategy, using fallback")
            val fallbackStrategy = NutritionStrategy(
                goal = user.workoutGoal,
                proteinPerKg = 2.0,
                fatRatio = 0.25,
                calorieAdjustment = 0.0,
                reasoning = "Fallback default strategy"
            )
            MacrosCalculator.fallback(user) to fallbackStrategy
        }
    }


    suspend fun getStrategy(user: UserProfileData): NutritionStrategy? {
        val systemPrompt = AIPrompts.initStrategyPrompt()
        val userContext = "User Profile: Age=${user.age.roundToInt()}, Weight=${user.weight.roundToInt()}${user.weightUnit}, Height=${user.height.roundToInt()}${user.heightUnit}, Sex=${user.sex}, Goal=${user.workoutGoal}, Activity=${user.activityLevel}"

        val request = OpenAIRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                OpenAIMessage("system", systemPrompt),
                OpenAIMessage("user", userContext)
            )
        )

        return try {
            val response = openAIService.getChatCompletions("Bearer ${BuildConfig.OPENAI_API_KEY}", request)
            val content = response.choices.firstOrNull()?.message?.content ?: return null
            val aiResponse = AIResponse.parse(content)
            if (aiResponse is AIResponse.NutritionStrategyResult) aiResponse.data else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching nutrition strategy: ${e.message}", e)
            null
        }
    }
}

fun buildUserContext(
    chatType: ChatType,
    user: UserProfileData,
    message: String,
    weeklyWorkouts: String,
    currentWorkoutPlan: String,
    currentMealPlan: String,
    weeklyMeals: String
): String {

    return buildString {
        append("User:\n")
        append("weight=${user.weight.roundToInt()}, goal=${user.workoutGoal}\n")

        when (chatType) {

            ChatType.WORKOUT -> {
                if (currentWorkoutPlan.isNotBlank())
                    append("Current workout: $currentWorkoutPlan\n")

                if (weeklyWorkouts.isNotBlank())
                    append("Weekly workouts: $weeklyWorkouts\n")
            }

            ChatType.MEALS -> {
                if (currentMealPlan.isNotBlank())
                    append("Current diet: $currentMealPlan\n")

                if (weeklyMeals.isNotBlank())
                    append("Weekly meals: $weeklyMeals\n")
            }

            ChatType.LOG_MEAL, ChatType.LOG_WORKOUT -> {
                // 🚫 No extra context → keep it LIGHT
            }
        }

        append("Time: ${Date().toTimeString()}\n")
        append("Message: $message")
    }
}
