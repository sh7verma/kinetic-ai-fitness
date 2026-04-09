package com.shverma.kinetic.data.preference

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shverma.kinetic.data.model.OnboardingData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton


private val Context.dataStore by preferencesDataStore(name = "kinetic_prefs")

@Singleton
class DataStoreHelper @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val ONBOARDING_DATA_KEY = stringPreferencesKey("onboarding_data")
    }

    private val dataStore = context.dataStore

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun saveEmail(email: String) {
        Log.d("DataStoreHelper", "Saving email: $email")
        dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
        }
    }

    val email: Flow<String?> = dataStore.data.map { preferences ->
        val savedEmail = preferences[EMAIL_KEY]
        Log.d("DataStoreHelper", "Retrieved email: $savedEmail")
        savedEmail
    }

    suspend fun saveOnboardingData(data: OnboardingData) {
        val jsonData = json.encodeToString(data)
        Log.d("DataStoreHelper", "Saving OnboardingData JSON: $jsonData")
        dataStore.edit { preferences ->
            preferences[ONBOARDING_DATA_KEY] = jsonData
        }
    }

    val onboardingData: Flow<OnboardingData?> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_DATA_KEY]?.let { jsonData ->
            Log.d("DataStoreHelper", "Retrieved OnboardingData JSON: $jsonData")
            try {
                json.decodeFromString<OnboardingData>(jsonData)
            } catch (e: Exception) {
                Log.e("DataStoreHelper", "Error decoding OnboardingData", e)
                null
            }
        }
    }

    suspend fun clearDataStore() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}