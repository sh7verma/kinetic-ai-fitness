package com.shverma.kinetic.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.preference.DataStoreHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface UserProfileRepository {
    suspend fun saveUserProfileData(data: UserProfileData)
    fun getUserProfileData(): Flow<UserProfileData?>
    suspend fun clearUserProfileData()
    suspend fun saveUserProfileToFirestore(user: UserProfileData)
    suspend fun fetchUserProfileFromFirestore(uid: String): UserProfileData?
    suspend fun deleteUserFromFirestore(uid: String)
}

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val dataStoreHelper: DataStoreHelper,
    private val firestore: FirebaseFirestore
) : UserProfileRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    override suspend fun saveUserProfileData(data: UserProfileData) {
        dataStoreHelper.saveUserProfileData(data)
    }

    override fun getUserProfileData(): Flow<UserProfileData?> {
        return dataStoreHelper.userProfileData
    }

    override suspend fun clearUserProfileData() {
        dataStoreHelper.clearDataStore()
    }

    override suspend fun saveUserProfileToFirestore(user: UserProfileData) {
        if (user.uid.isBlank()) return
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(user)
            .await()
    }

    override suspend fun fetchUserProfileFromFirestore(uid: String): UserProfileData? {
        if (uid.isBlank()) return null
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()
            val data = snapshot.toObject(UserProfileData::class.java)
            Log.d("UserProfileRepository", "Fetched UserProfileData from Firestore: $data")
            data
        } catch (e: Exception) {
            Log.e("UserProfileRepository", "Error fetching UserProfileData from Firestore: ${e.message}", e)
            null
        }
    }

    override suspend fun deleteUserFromFirestore(uid: String) {
        if (uid.isBlank()) return
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .delete()
            .await()
    }
}
