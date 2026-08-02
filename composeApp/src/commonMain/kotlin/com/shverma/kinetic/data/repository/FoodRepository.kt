package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.model.ai.AIFoodItem
import kotlinx.coroutines.flow.last

class FoodRepository(
    private val localDataSource: LocalFoodDataSource,
    private val remoteDataSource: FoodRemoteDataSource,
    private val foodResolver: FoodResolver
) {

    suspend fun syncFoodsFromFirestore() {
        val foods = remoteDataSource.fetchAllFoods()
        localDataSource.saveFoods(foods)
    }

    suspend fun searchFoods(query: String): List<FoodEntity> {
        return localDataSource.search(query)
    }

    suspend fun getFood(item: String): FoodEntity? {
        val resolved = foodResolver.resolve(item)
        val local = localDataSource.findBestFood(resolved)
        if (local != null) return local

        // If not found locally, use resolveFoods to potentially fetch/estimate and save
        val resolvedMap = foodResolver.resolveFoods(listOf(AIFoodItem(food = item))).last()
        return resolvedMap.values.firstOrNull()
    }
}
