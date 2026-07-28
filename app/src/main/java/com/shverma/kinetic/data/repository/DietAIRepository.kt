package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.model.ai.AILogResponse
import com.shverma.kinetic.data.network.FoodAIService
import com.shverma.kinetic.data.preference.DataStoreHelper
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

interface DietAIRepository {
    suspend fun logFood(
        message: String
    ): AILogResponse
}

@Singleton
class DietAIRepositoryImpl @Inject constructor(
    private val dataStoreHelper: DataStoreHelper,
    private val foodAIService: FoodAIService
) : DietAIRepository {

    override suspend fun logFood(
        message: String
    ): AILogResponse {
        val user = dataStoreHelper.userProfileData.firstOrNull() ?: UserProfileData()
        return foodAIService.logFood(user, message)
    }
}