package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.model.ai.AIDietPlanResponse
import com.shverma.kinetic.data.model.ai.AILogResponse
import com.shverma.kinetic.data.network.FoodAIService
import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.utils.Resource
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

interface DietAIRepository {
    suspend fun createDietPlan(
        message: String
    ): Resource<AIDietPlanResponse>

    suspend fun logFood(
        message: String
    ): AILogResponse
}

@Singleton
class DietAIRepositoryImpl @Inject constructor(
    private val dataStoreHelper: DataStoreHelper,
    private val foodAIService: FoodAIService
) : DietAIRepository {

    override suspend fun createDietPlan(
        message: String
    ): Resource<AIDietPlanResponse> {
        val user = dataStoreHelper.userProfileData.firstOrNull() ?: UserProfileData()
        return foodAIService.getCreateDietPlan(user, message)
    }

    override suspend fun logFood(
        message: String
    ): AILogResponse {
        val user = dataStoreHelper.userProfileData.firstOrNull() ?: UserProfileData()
        return foodAIService.logFood(user, message)
    }
}