package com.shverma.kinetic.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.local.entity.FoodSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFoodDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "RemoteFoodDataSource"
    }

    suspend fun fetchFoodsBatch(names: List<String>): List<FoodEntity> {
        val results = mutableListOf<FoodEntity>()

        for (name in names) {
            val entity = fetchFood(name)
            if (entity != null) {
                results.add(entity)
            }
        }

        return results
    }

    suspend fun fetchFood(name: String): FoodEntity? {
        return try {
            val doc = firestore.collection("foods")
                .document(name)
                .get()
                .await()

            if (!doc.exists()) return null

            FoodEntity(
                foodId = doc.getLong("foodId")?.toInt() ?: 0,
                name = doc.getString("name") ?: name,
                caloriesPer100g = doc.getDouble("caloriesPer100g") ?: 0.0,
                proteinPer100g = doc.getDouble("proteinPer100g") ?: 0.0,
                carbsPer100g = doc.getDouble("carbsPer100g") ?: 0.0,
                fatsPer100g = doc.getDouble("fatsPer100g") ?: 0.0,
                source = FoodSource.from(doc.getString("source"))
            )
        } catch (e: Exception) {
            Log.e(TAG, "Firestore error for $name: ${e.message}")
            null
        }
    }

    suspend fun fetchAllFoods(): List<FoodEntity> {
        return try {
            val snapshot = firestore.collection("foods")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null

                FoodEntity(
                    foodId = doc.getLong("foodId")?.toInt() ?: 0,
                    name = name,
                    caloriesPer100g = doc.getDouble("caloriesPer100g") ?: doc.getDouble("calories") ?: 0.0,
                    proteinPer100g = doc.getDouble("proteinPer100g") ?: doc.getDouble("protein") ?: 0.0,
                    carbsPer100g = doc.getDouble("carbsPer100g") ?: doc.getDouble("carbs") ?: 0.0,
                    fatsPer100g = doc.getDouble("fatsPer100g") ?: doc.getDouble("fats") ?: 0.0,
                    source = FoodSource.from(doc.getString("source")),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore fetch all error: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveFood(food: FoodEntity) {
        try {
            firestore.collection("foods")
                .document(food.name)
                .set(food)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving food to Firestore: ${e.message}")
        }
    }
}
