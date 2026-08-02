package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.local.entity.FoodEntity

interface FoodRemoteDataSource {
    suspend fun fetchFoodsBatch(names: List<String>): List<FoodEntity>
    suspend fun fetchAllFoods(): List<FoodEntity>
    suspend fun saveFood(food: FoodEntity)
}
