package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.entity.FoodEntity

class LocalFoodDataSource(
    private val foodDao: FoodDao
) {
    suspend fun getFoodsByNames(names: List<String>): List<FoodEntity> {
        return foodDao.findFoodsByNames(names)
    }

    suspend fun saveFood(food: FoodEntity): Int {
        return foodDao.insert(food).toInt()
    }

    suspend fun saveFoods(foods: List<FoodEntity>) {
        foodDao.insertAll(foods)
    }

    suspend fun search(query: String): List<FoodEntity> {
        return foodDao.searchFoods(query)
    }

    suspend fun findBestFood(query: String): FoodEntity? {
        return foodDao.findBestFood(query)
    }
}
