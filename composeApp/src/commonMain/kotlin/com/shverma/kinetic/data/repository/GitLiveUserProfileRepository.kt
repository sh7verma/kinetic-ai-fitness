package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.network.FoodAIService
import com.shverma.kinetic.data.preference.KineticPreferences
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow

/** Shared profile repository backed by GitLive Firestore and KMP Preferences DataStore. */
class GitLiveUserProfileRepository(
    private val preferences: KineticPreferences,
    private val foodAIService: FoodAIService,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : UserProfileRepository {
    override suspend fun saveUserProfileData(data: UserProfileData) {
        preferences.saveUserProfileData(data)
    }

    override fun getUserProfileData(): Flow<UserProfileData?> = preferences.userProfileData

    override suspend fun clearUserProfileData() {
        preferences.clearDataStore()
    }

    override suspend fun saveUserProfileToFirestore(user: UserProfileData) {
        if (user.uid.isBlank()) return
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(user)
    }

    override suspend fun fetchUserProfileFromFirestore(uid: String): UserProfileData? {
        if (uid.isBlank()) return null
        return runCatching {
            val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get()
            if (snapshot.exists) snapshot.data<UserProfileData>() else null
        }.getOrNull()
    }

    override suspend fun deleteUserFromFirestore(uid: String) {
        if (uid.isBlank()) return
        firestore.collection(USERS_COLLECTION).document(uid).delete()
    }

    override suspend fun getInitialTargetCalories(user: UserProfileData) =
        foodAIService.getInitialTargetCalories(user)

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
