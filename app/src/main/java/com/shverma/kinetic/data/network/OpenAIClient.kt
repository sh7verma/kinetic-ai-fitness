package com.shverma.kinetic.data.network

import android.util.Log
import com.shverma.kinetic.BuildConfig
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.utils.toTimeString
import java.util.Date
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
        append("weight=${user.weight}, goal=${user.workoutGoal}\n")

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
