package com.shverma.kinetic.data.repository

import android.util.Log
import com.shverma.kinetic.data.model.AIResponse
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.network.ChatType
import com.shverma.kinetic.data.network.OpenAIClient
import com.shverma.kinetic.data.preference.DataStoreHelper
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface ChatRepository {
    suspend fun chat(
        message: String,
        chatType: ChatType,
        weeklyWorkouts: String = "",
        currentWorkoutPlan: String = "",
        currentMealPlan: String = "",
        weeklyMeals: String = ""
    ): Result<AIResponse>
}

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val dataStoreHelper: DataStoreHelper,
    private val openAIClient: OpenAIClient,
    private val mealPlanner: MealPlanner
) : ChatRepository {

    private val TAG = "ChatRepository"

    override suspend fun chat(
        message: String,
        chatType: ChatType,
        weeklyWorkouts: String,
        currentWorkoutPlan: String,
        currentMealPlan: String,
        weeklyMeals: String
    ): Result<AIResponse> {
        Log.d(TAG, "Chat request: $message, Type: $chatType")
        return try {
            val user = dataStoreHelper.userProfileData.firstOrNull() ?: UserProfileData()

            if (chatType == ChatType.MEALS) {
                Log.d(TAG, "Using MealPlanner for MEALS chat type")
                val mealPlan = mealPlanner.getMealPlanFromAI(user, message)
                return Result.success(AIResponse.MealPlanResult(mealPlan))
            }

            Log.d(TAG, "Using OpenAI provider")
            val (response, usage) = openAIClient.chatWithUsage(
                user = user,
                message = message,
                chatType = chatType,
                weeklyWorkouts = weeklyWorkouts,
                currentWorkoutPlan = currentWorkoutPlan,
                currentMealPlan = currentMealPlan,
                weeklyMeals = weeklyMeals
            )
            Log.d(TAG,"OpenAI Usage: Input Tokens: ${usage?.promptTokens}, Output Tokens: ${usage?.completionTokens}, Total Tokens: ${usage?.totalTokens}")
            val text = response?.takeIf { it.isNotBlank() }
                ?: throw Exception("Empty or blank response from OpenAI")

            Log.d(TAG, "Chat response raw text: $text")
            val parsed = AIResponse.parse(text)
            Log.d(TAG, "Chat response parsed: $parsed")
            Result.success(parsed)
        } catch (e: Exception) {
            Log.e(TAG, "Chat error: ${e.message}", e)
            Result.failure(handleError(e))
        }
    }

    private fun handleError(e: Exception): Exception {
        return e
    }
}