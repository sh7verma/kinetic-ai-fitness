package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.local.dao.MealDao
import com.shverma.kinetic.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface MealRepository {
    fun getAllMeals(): Flow<List<MealEntity>>
    fun getMealsByDate(date: String): Flow<List<MealEntity>>
    fun getMealsFromDate(startDate: String): Flow<List<MealEntity>>
    suspend fun insertMeal(meal: MealEntity)
    suspend fun deleteMeal(meal: MealEntity)
    suspend fun deleteMealById(id: Long)
}

@Singleton
class MealRepositoryImpl @Inject constructor(
    private val mealDao: MealDao
) : MealRepository {
    override fun getAllMeals(): Flow<List<MealEntity>> = mealDao.getAllMeals()

    override fun getMealsByDate(date: String): Flow<List<MealEntity>> = mealDao.getMealsByDate(date)

    override fun getMealsFromDate(startDate: String): Flow<List<MealEntity>> = mealDao.getMealsFromDate(startDate)

    override suspend fun insertMeal(meal: MealEntity) {
        mealDao.insertMeal(meal)
    }

    override suspend fun deleteMeal(meal: MealEntity) {
        mealDao.deleteMeal(meal)
    }

    override suspend fun deleteMealById(id: Long) {
        mealDao.deleteMealById(id)
    }
}
