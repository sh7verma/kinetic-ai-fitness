package com.shverma.kinetic.data.preference

import android.content.Context
import androidx.datastore.preferences.preferencesDataStoreFile
import com.shverma.kinetic.data.model.UserProfileData
import kotlinx.coroutines.flow.Flow

class DataStoreHelper(
    context: Context,
) : KineticPreferences {
    private val delegate = KineticDataStore(
        createKineticDataStore(
            context.preferencesDataStoreFile("kinetic_prefs").absolutePath,
        ),
    )

    override suspend fun saveEmail(email: String) = delegate.saveEmail(email)

    override val email: Flow<String?> = delegate.email

    override suspend fun saveUserProfileData(data: UserProfileData) = delegate.saveUserProfileData(data)

    override val userProfileData: Flow<UserProfileData?> = delegate.userProfileData

    override suspend fun clearDataStore() = delegate.clearDataStore()

    override suspend fun saveLastFoodSync(timestamp: Long) = delegate.saveLastFoodSync(timestamp)

    override val lastFoodSync: Flow<Long> = delegate.lastFoodSync
}
