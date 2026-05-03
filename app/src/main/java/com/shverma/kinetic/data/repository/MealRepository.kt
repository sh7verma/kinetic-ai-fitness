package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.local.dao.MealDao
import com.shverma.kinetic.data.local.entity.MealEntity
import com.shverma.kinetic.data.model.MealPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface MealRepository {
    fun getAllMeals(): Flow<List<MealEntity>>
    fun getMealsByDate(date: String): Flow<List<MealEntity>>
    fun getMealsFromDate(startDate: String): Flow<List<MealEntity>>
    suspend fun insertMeal(meal: MealEntity)
    suspend fun deleteMeal(meal: MealEntity)
    suspend fun deleteMealById(id: Long)
    suspend fun saveMealPlanToFirestore(uid: String, mealPlan: MealPlan)
    suspend fun getMealPlanFromFirestore(uid: String, date: String): MealPlan?
}

@Singleton
class MealRepositoryImpl @Inject constructor(
    private val mealDao: MealDao,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore
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

    override suspend fun saveMealPlanToFirestore(uid: String, mealPlan: MealPlan) {
        if (uid.isBlank()) return
        try {
            firestore.collection("users")
                .document(uid)
                .collection("meal_plans")
                .document(mealPlan.date)
                .set(mealPlan)
                .addOnSuccessListener {
                    android.util.Log.d("MealRepository", "Meal plan saved to Firestore for date: ${mealPlan.date}")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("MealRepository", "Error saving meal plan to Firestore", e)
                }
        } catch (e: Exception) {
            android.util.Log.e("MealRepository", "Exception saving meal plan to Firestore", e)
        }
    }

    override suspend fun getMealPlanFromFirestore(uid: String, date: String): MealPlan? {
        if (uid.isBlank()) return null
        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("meal_plans")
                .document(date)
                .get()
                .await()
            snapshot.toObject(MealPlan::class.java)
        } catch (e: Exception) {
            android.util.Log.e("MealRepository", "Error fetching meal plan from Firestore", e)
            null
        }
    }
}
