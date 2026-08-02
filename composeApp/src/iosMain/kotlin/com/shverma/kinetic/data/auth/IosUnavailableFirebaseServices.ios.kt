package com.shverma.kinetic.data.auth

import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.model.ai.NutritionStrategy
import com.shverma.kinetic.data.model.ai.TargetCaloriesData
import com.shverma.kinetic.data.network.FoodAIService
import com.shverma.kinetic.data.preference.KineticPreferences
import com.shverma.kinetic.data.repository.FoodRemoteDataSource
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.ui.welcome.AuthUser
import kotlinx.coroutines.flow.Flow

/** Safe startup implementations used only until the host supplies Firebase iOS configuration. */
internal class IosUnavailableAuthSession : AuthSession {
    override fun currentUser(): AuthUser? = null

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthUser? = null

    override suspend fun signInAnonymously(): AuthUser? = null

    override suspend fun signOut() = Unit

    override suspend fun deleteAccount() = Unit
}

internal class IosUnavailableFoodRemoteDataSource : FoodRemoteDataSource {
    override suspend fun fetchFoodsBatch(names: List<String>): List<FoodEntity> = emptyList()

    override suspend fun fetchAllFoods(): List<FoodEntity> = emptyList()

    override suspend fun saveFood(food: FoodEntity) = Unit
}

internal class IosUnavailableUserProfileRepository(
    private val preferences: KineticPreferences,
    private val foodAIService: FoodAIService,
) : UserProfileRepository {
    override suspend fun saveUserProfileData(data: UserProfileData) {
        preferences.saveUserProfileData(data)
    }

    override fun getUserProfileData(): Flow<UserProfileData?> = preferences.userProfileData

    override suspend fun clearUserProfileData() {
        preferences.clearDataStore()
    }

    override suspend fun saveUserProfileToFirestore(user: UserProfileData) = Unit

    override suspend fun fetchUserProfileFromFirestore(uid: String): UserProfileData? = null

    override suspend fun deleteUserFromFirestore(uid: String) = Unit

    override suspend fun getInitialTargetCalories(user: UserProfileData):
        Pair<TargetCaloriesData, NutritionStrategy>? = foodAIService.getInitialTargetCalories(user)
}
