package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.data.model.OnboardingData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface OnboardingRepository {
    suspend fun saveOnboardingData(data: OnboardingData)
    fun getOnboardingData(): Flow<OnboardingData?>
    suspend fun clearOnboardingData()
}

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    private val dataStoreHelper: DataStoreHelper
) : OnboardingRepository {
    override suspend fun saveOnboardingData(data: OnboardingData) {
        dataStoreHelper.saveOnboardingData(data)
    }

    override fun getOnboardingData(): Flow<OnboardingData?> {
        return dataStoreHelper.onboardingData
    }

    override suspend fun clearOnboardingData() {
        dataStoreHelper.clearDataStore()
    }
}
