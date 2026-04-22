package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.data.model.UserProfileData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface UserProfileRepository {
    suspend fun saveUserProfileData(data: UserProfileData)
    fun getUserProfileData(): Flow<UserProfileData?>
    suspend fun clearUserProfileData()
}

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val dataStoreHelper: DataStoreHelper
) : UserProfileRepository {
    override suspend fun saveUserProfileData(data: UserProfileData) {
        dataStoreHelper.saveUserProfileData(data)
    }

    override fun getUserProfileData(): Flow<UserProfileData?> {
        return dataStoreHelper.userProfileData
    }

    override suspend fun clearUserProfileData() {
        dataStoreHelper.clearDataStore()
    }
}
