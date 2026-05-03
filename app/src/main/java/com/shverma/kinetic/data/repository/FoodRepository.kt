package com.shverma.kinetic.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.local.entity.FoodSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val foodDao: FoodDao,
    private val foodResolver: FoodResolver
) {

    suspend fun syncFoodsFromFirestore() {
        val snapshot = firestore.collection("foods")
            .get()
            .await()

        val foods = snapshot.documents.mapNotNull { doc ->
            val name = doc.getString("name") ?: return@mapNotNull null

            FoodEntity(
                name = name,
                calories = doc.getDouble("calories") ?: 0.0,
                protein = doc.getDouble("protein") ?: 0.0,
                carbs = doc.getDouble("carbs") ?: 0.0,
                fats = doc.getDouble("fats") ?: 0.0,
                source = FoodSource.from(doc.getString("source"))
                    ?: FoodSource.estimated,
                quantity = doc.getString("quantity") ?: "",
                unit = doc.getString("unit") ?: ""
            )
        }

        foodDao.insertAll(foods)
    }

    suspend fun searchFoods(query: String): List<FoodEntity> {
        return foodDao.searchFoods(query)
    }

    suspend fun getFood(item: String): FoodEntity? {
        val resolved = foodResolver.resolve(item)
        return foodDao.findBestFood(resolved)
    }
}
