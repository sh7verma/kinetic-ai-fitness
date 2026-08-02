package com.shverma.kinetic.data.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.shverma.kinetic.data.model.UserProfileData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

expect fun createKineticDataStore(filePath: String): DataStore<Preferences>

interface KineticPreferences {
    suspend fun saveEmail(email: String)
    val email: Flow<String?>
    suspend fun saveUserProfileData(data: UserProfileData)
    val userProfileData: Flow<UserProfileData?>
    suspend fun clearDataStore()
    suspend fun saveLastFoodSync(timestamp: Long)
    val lastFoodSync: Flow<Long>
}

class KineticDataStore(
    private val dataStore: DataStore<Preferences>,
) : KineticPreferences {
    private val emailKey = stringPreferencesKey("email")
    private val userProfileDataKey = stringPreferencesKey("user_profile_data")
    private val lastFoodSyncKey = stringPreferencesKey("last_food_sync")

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override suspend fun saveEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[emailKey] = email
        }
    }

    override val email: Flow<String?> = dataStore.data.map { preferences ->
        preferences[emailKey]
    }

    override suspend fun saveUserProfileData(data: UserProfileData) {
        dataStore.edit { preferences ->
            preferences[userProfileDataKey] = json.encodeToString(data)
        }
    }

    override val userProfileData: Flow<UserProfileData?> = dataStore.data.map { preferences ->
        preferences[userProfileDataKey]?.let { jsonData ->
            runCatching { json.decodeFromString<UserProfileData>(jsonData) }.getOrNull()
        }
    }

    override suspend fun clearDataStore() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    override suspend fun saveLastFoodSync(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[lastFoodSyncKey] = timestamp.toString()
        }
    }

    override val lastFoodSync: Flow<Long> = dataStore.data.map { preferences ->
        preferences[lastFoodSyncKey]?.toLongOrNull() ?: 0L
    }
}
