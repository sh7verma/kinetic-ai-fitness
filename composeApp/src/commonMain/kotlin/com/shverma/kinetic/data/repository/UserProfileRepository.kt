package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.model.ai.NutritionStrategy
import com.shverma.kinetic.data.model.ai.TargetCaloriesData
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    suspend fun saveUserProfileData(data: UserProfileData)
    fun getUserProfileData(): Flow<UserProfileData?>
    suspend fun clearUserProfileData()
    suspend fun saveUserProfileToFirestore(user: UserProfileData)
    suspend fun fetchUserProfileFromFirestore(uid: String): UserProfileData?
    suspend fun deleteUserFromFirestore(uid: String)
    suspend fun getInitialTargetCalories(user: UserProfileData): Pair<TargetCaloriesData, NutritionStrategy>?
}
