package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.model.ai.AILogResponse
import com.shverma.kinetic.data.network.FoodAIService
import com.shverma.kinetic.data.preference.KineticPreferences
import kotlinx.coroutines.flow.firstOrNull

interface DietAIRepository {
    suspend fun logFood(
        message: String
    ): AILogResponse
}

class DietAIRepositoryImpl(
    private val preferences: KineticPreferences,
    private val foodAIService: FoodAIService
) : DietAIRepository {

    override suspend fun logFood(
        message: String
    ): AILogResponse {
        val user = preferences.userProfileData.firstOrNull() ?: UserProfileData()
        return foodAIService.logFood(user, message)
    }
}
