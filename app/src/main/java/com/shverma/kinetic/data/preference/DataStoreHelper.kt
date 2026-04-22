package com.shverma.kinetic.data.preference

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shverma.kinetic.data.model.UserProfileData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton


private val Context.dataStore by preferencesDataStore(name = "kinetic_prefs")

@Singleton
class DataStoreHelper @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val USER_PROFILE_DATA_KEY = stringPreferencesKey("user_profile_data")
        private val CURRENT_MEAL_PLAN_KEY = stringPreferencesKey("current_meal_plan")
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

    suspend fun saveUserProfileData(data: UserProfileData) {
        val jsonData = json.encodeToString(data)
        Log.d("DataStoreHelper", "Saving UserProfileData JSON: $jsonData")
        dataStore.edit { preferences ->
            preferences[USER_PROFILE_DATA_KEY] = jsonData
        }
    }

    val userProfileData: Flow<UserProfileData?> = dataStore.data.map { preferences ->
        preferences[USER_PROFILE_DATA_KEY]?.let { jsonData ->
            Log.d("DataStoreHelper", "Retrieved UserProfileData JSON: $jsonData")
            try {
                json.decodeFromString<UserProfileData>(jsonData)
            } catch (e: Exception) {
                Log.e("DataStoreHelper", "Error decoding UserProfileData", e)
                null
            }
        }
    }

    suspend fun clearDataStore() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun saveMealPlan(plan: com.shverma.kinetic.data.model.MealPlan) {
        val jsonData = json.encodeToString(plan)
        dataStore.edit { preferences ->
            preferences[CURRENT_MEAL_PLAN_KEY] = jsonData
        }
    }

    val mealPlan: Flow<com.shverma.kinetic.data.model.MealPlan?> = dataStore.data.map { preferences ->
        preferences[CURRENT_MEAL_PLAN_KEY]?.let { jsonData ->
            try {
                json.decodeFromString<com.shverma.kinetic.data.model.MealPlan>(jsonData)
            } catch (e: Exception) {
                Log.e("DataStoreHelper", "Error decoding MealPlan", e)
                null
            }
        }
    }
}