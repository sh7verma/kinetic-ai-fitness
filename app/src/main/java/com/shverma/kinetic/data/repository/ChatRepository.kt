package com.shverma.kinetic.data.repository

import android.util.Log
import com.shverma.kinetic.data.model.AIResponse
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.network.OpenAIClient
import com.shverma.kinetic.data.preference.DataStoreHelper
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface ChatRepository {
    suspend fun chat(
        message: String,
        recentWorkouts: String = "",
        todaysWorkout: String = ""
    ): Result<AIResponse>
}

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val dataStoreHelper: DataStoreHelper
) : ChatRepository {

    private val TAG = "ChatRepository"

    override suspend fun chat(
        message: String,
        recentWorkouts: String,
        todaysWorkout: String
    ): Result<AIResponse> {
        Log.d(TAG, "Chat request: $message")
        return try {
            val user = dataStoreHelper.userProfileData.firstOrNull() ?: UserProfileData()

            Log.d(TAG, "Using OpenAI provider")
            val (response, usage) = OpenAIClient.chatWithUsage(user, message, recentWorkouts, todaysWorkout)
            Timber.tag(TAG)
                .d("OpenAI Usage: Input Tokens: ${usage?.promptTokens}, Output Tokens: ${usage?.completionTokens}, Total Tokens: ${usage?.totalTokens}")
            val text = response ?: throw Exception("Empty response from OpenAI")

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