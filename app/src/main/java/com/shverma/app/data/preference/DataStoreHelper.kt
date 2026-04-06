package com.shverma.app.data.preference

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton


private val Context.dataStore by preferencesDataStore(name = "")

@Singleton
class DataStoreHelper @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val EMAIL_KEY = stringPreferencesKey("email")
    }

    private val dataStore = context.dataStore

    suspend fun saveEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
        }
    }

    val email: Flow<String?> = dataStore.data.map { preferences ->
        preferences[EMAIL_KEY]
    }

    suspend fun clearDataStore() {
        runBlocking {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }
}