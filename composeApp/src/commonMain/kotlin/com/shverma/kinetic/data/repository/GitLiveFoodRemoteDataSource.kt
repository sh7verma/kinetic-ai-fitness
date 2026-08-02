package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.local.entity.FoodSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.serialization.Serializable

/** Shared Firestore food-catalog adapter backed by the GitLive Firebase SDK. */
class GitLiveFoodRemoteDataSource(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : FoodRemoteDataSource {
    override suspend fun fetchFoodsBatch(names: List<String>): List<FoodEntity> = names.mapNotNull { name ->
        fetchFood(name)
    }

    override suspend fun fetchAllFoods(): List<FoodEntity> = firestore
        .collection(FOODS_COLLECTION)
        .get()
        .documents
        .mapNotNull { snapshot ->
            if (!snapshot.exists) return@mapNotNull null
            runCatching { snapshot.data<FirestoreFoodRecord>().toFoodEntity() }.getOrNull()
        }

    override suspend fun saveFood(food: FoodEntity) {
        firestore.collection(FOODS_COLLECTION)
            .document(food.name)
            .set(food.toFirestoreRecord())
    }

    private suspend fun fetchFood(name: String): FoodEntity? {
        val snapshot = firestore.collection(FOODS_COLLECTION).document(name).get()
        if (!snapshot.exists) return null
        return runCatching { snapshot.data<FirestoreFoodRecord>().toFoodEntity(name) }.getOrNull()
    }

    private companion object {
        const val FOODS_COLLECTION = "foods"
    }
}

@Serializable
private data class FirestoreFoodRecord(
    val foodId: Int = 0,
    val name: String = "",
    val caloriesPer100g: Double? = null,
    val proteinPer100g: Double? = null,
    val carbsPer100g: Double? = null,
    val fatsPer100g: Double? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fats: Double? = null,
    val source: String? = null,
    val updatedAt: Long = 0L,
)

private fun FirestoreFoodRecord.toFoodEntity(fallbackName: String = name): FoodEntity = FoodEntity(
    foodId = foodId,
    name = name.ifBlank { fallbackName },
    caloriesPer100g = caloriesPer100g ?: calories ?: 0.0,
    proteinPer100g = proteinPer100g ?: protein ?: 0.0,
    carbsPer100g = carbsPer100g ?: carbs ?: 0.0,
    fatsPer100g = fatsPer100g ?: fats ?: 0.0,
    source = FoodSource.from(source),
    updatedAt = updatedAt,
)

private fun FoodEntity.toFirestoreRecord(): FirestoreFoodRecord = FirestoreFoodRecord(
    foodId = foodId,
    name = name,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    carbsPer100g = carbsPer100g,
    fatsPer100g = fatsPer100g,
    source = source.name,
    updatedAt = updatedAt,
)
