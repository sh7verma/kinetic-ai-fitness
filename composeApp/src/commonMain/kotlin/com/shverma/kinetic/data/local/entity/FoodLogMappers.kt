package com.shverma.kinetic.data.local.entity

import com.shverma.kinetic.data.model.ai.AILogResponse
import com.shverma.kinetic.data.model.ai.displayMealType
import com.shverma.kinetic.data.model.ai.safeGrams
import com.shverma.kinetic.data.repository.FoodResolver
import com.shverma.kinetic.ui.aichat.UIFoodItem
import com.shverma.kinetic.ui.aichat.UILog
import com.shverma.kinetic.ui.aichat.UIMeal
import com.shverma.kinetic.utils.currentTimeMillis
import kotlinx.coroutines.flow.last

suspend fun AILogResponse.toFoodLogs(
    foodResolver: FoodResolver,
    dietPlanId: Long? = null,
): List<FoodLogEntity> {
    val logs = mutableListOf<FoodLogEntity>()

    for (entry in entries) {
        val foodMap = foodResolver.resolveFoods(entry.items).last()

        for (item in entry.items) {
            val resolvedName = foodResolver.resolve(item.food)
            val food = foodMap[resolvedName] ?: continue
            val grams = safeGrams(item.grams!!)

            logs.add(
                FoodLogEntity(
                    foodId = food.foodId,
                    grams = grams,
                    timestamp = currentTimeMillis(),
                    mealType = entry.displayMealType,
                    dietPlanId = dietPlanId,
                ),
            )
        }
    }

    return logs
}

suspend fun AILogResponse.toUILog(foodResolver: FoodResolver): UILog {
    val uiMeals = entries.map { entry ->
        val foodMap = foodResolver.resolveFoods(entry.items).last()

        val uiFoodItems = entry.items.mapNotNull { item ->
            val resolvedName = foodResolver.resolve(item.food)
            val food = foodMap[resolvedName] ?: return@mapNotNull null

            val grams = safeGrams(item.grams!!)
            val factor = grams / 100.0

            UIFoodItem(
                name = food.name,
                grams = grams,
                calories = food.caloriesPer100g * factor,
                protein = food.proteinPer100g * factor,
                carbs = food.carbsPer100g * factor,
                fats = food.fatsPer100g * factor,
                confidence = item.confidence,
                assumed = item.assumed,
            )
        }

        UIMeal(
            mealType = entry.displayMealType,
            items = uiFoodItems,
            totalCalories = uiFoodItems.sumOf { it.calories },
            totalProtein = uiFoodItems.sumOf { it.protein },
            totalCarbs = uiFoodItems.sumOf { it.carbs },
            totalFats = uiFoodItems.sumOf { it.fats },
        )
    }

    return UILog(meals = uiMeals)
}
