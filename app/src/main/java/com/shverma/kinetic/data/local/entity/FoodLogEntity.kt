package com.shverma.kinetic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shverma.kinetic.data.model.ai.AIDietPlan
import com.shverma.kinetic.data.model.ai.AILogResponse
import com.shverma.kinetic.data.model.ai.displayMealType
import com.shverma.kinetic.data.model.ai.safeGrams
import com.shverma.kinetic.data.repository.FoodResolver
import com.shverma.kinetic.ui.aichat.UIFoodItem
import com.shverma.kinetic.ui.aichat.UILog
import com.shverma.kinetic.ui.aichat.UIMeal
import timber.log.Timber
import androidx.room.Embedded
import androidx.room.Relation

import kotlinx.coroutines.flow.last

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val foodId: Int,

    val grams: Double,

    val timestamp: Long? = null,     // exact time user ate
    val mealType: String,    // Breakfast|Lunch|Dinner|Pre-workout|Post-workout|Snack

    // optional link
    val dietPlanId: Long? = null,

    val createdAt: Long = System.currentTimeMillis()
)

data class FoodLogWithFood(
    @Embedded val log: FoodLogEntity,
    @Relation(
        parentColumn = "foodId",
        entityColumn = "foodId"
    )
    val food: FoodEntity
)


suspend fun AIDietPlan.toFoodLogs(
    dietPlanId: Long,
    foodResolver: FoodResolver
): List<FoodLogEntity> {

    val logs = mutableListOf<FoodLogEntity>()

    for (meal in meals) {
        val foodMap = foodResolver.resolveFoods(meal.items).last()

        for (item in meal.items) {
            val resolvedName = foodResolver.resolve(item.food)

            val food = foodMap[resolvedName]
                ?: continue // skip if something went wrong

            val grams = safeGrams(item.grams!!)

            logs.add(
                FoodLogEntity(
                    foodId = food.foodId,
                    grams = grams,
                    timestamp = System.currentTimeMillis(),
                    mealType = meal.name,
                    dietPlanId = dietPlanId
                )
            )
        }
    }
    return logs
}


suspend fun AILogResponse.toFoodLogs(
    foodResolver: FoodResolver,
    dietPlanId: Long? = null
): List<FoodLogEntity> {

    val logs = mutableListOf<FoodLogEntity>()

    for (entry in entries) {
        // Resolve all foods in this meal
        val foodMap = foodResolver.resolveFoods(entry.items).last()

        for (item in entry.items) {

            val resolvedName = foodResolver.resolve(item.food)

            val food = foodMap[resolvedName]
                ?: continue

            val grams = safeGrams(item.grams!!)

            logs.add(
                FoodLogEntity(
                    foodId = food.foodId,
                    grams = grams,
                    timestamp = System.currentTimeMillis(), // or pass custom
                    mealType = entry.displayMealType,
                    dietPlanId = dietPlanId
                )
            )
        }
    }

    return logs
}


suspend fun AILogResponse.toUILog(foodResolver: FoodResolver): UILog {
    Timber.d("toUILog: Parsing AILogResponse with ${entries.size} entries")
    val uiMeals = entries.map { entry ->
        Timber.d("toUILog: Resolving foods for meal type: ${entry.mealType}")
        val foodMap = foodResolver.resolveFoods(entry.items).last()

        val uiFoodItems = entry.items.mapNotNull { item ->
            val resolvedName = foodResolver.resolve(item.food)
            val food = foodMap[resolvedName] ?: run {
                Timber.w("toUILog: Could not resolve food: ${item.food}")
                return@mapNotNull null
            }

            val grams = safeGrams(item.grams!!)
            val factor = grams / 100.0

            UIFoodItem(
                name = food.name,
                grams = grams,
                calories = food.caloriesPer100g * factor,
                protein = food.proteinPer100g * factor,
                carbs = food.carbsPer100g * factor,
                fats = food.fatsPer100g * factor
            ).also {
                Timber.v("toUILog: Created UIFoodItem: ${it.name} (${it.grams}g)")
            }
        }

        UIMeal(
            mealType = entry.displayMealType,
            items = uiFoodItems,
            totalCalories = uiFoodItems.sumOf { it.calories },
            totalProtein = uiFoodItems.sumOf { it.protein },
            totalCarbs = uiFoodItems.sumOf { it.carbs },
            totalFats = uiFoodItems.sumOf { it.fats }
        )
    }

    return UILog(meals = uiMeals).also {
        Timber.d("toUILog: Successfully parsed UILog with ${it.meals.size} meals")
    }
}