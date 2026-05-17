package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.local.entity.FoodSource
import com.shverma.kinetic.data.model.ai.AIFoodItem
import com.shverma.kinetic.data.network.FoodAIService
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AINutritionDataSource @Inject constructor(
    private val foodAIService: FoodAIService
) {
    suspend fun estimateNutrition(names: List<String>): List<FoodEntity> {
        Timber.d("estimateNutrition: Estimating for: $names")
        val aiItems = foodAIService.getNutritionPer100g(
            names.map { AIFoodItem(food = it) }
        )

        Timber.d("estimateNutrition: AI returned ${aiItems.size} items")

        return aiItems.map { ai ->
            FoodEntity(
                name = ai.name,
                caloriesPer100g = ai.caloriesPer100g,
                proteinPer100g = ai.proteinPer100g,
                carbsPer100g = ai.carbsPer100g,
                fatsPer100g = ai.fatsPer100g,
                source = FoodSource.ai
            ).also {
                Timber.v("estimateNutrition: Mapped AI item: ${it.name}")
            }
        }
    }
}
