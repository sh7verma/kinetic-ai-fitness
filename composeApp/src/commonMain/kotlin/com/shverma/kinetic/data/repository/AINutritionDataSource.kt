package com.shverma.kinetic.data.repository

import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.local.entity.FoodSource
import com.shverma.kinetic.data.model.ai.AIFoodItem
import com.shverma.kinetic.data.network.FoodAIService

class AINutritionDataSource(
    private val foodAIService: FoodAIService
) {
    suspend fun estimateNutrition(names: List<String>): List<FoodEntity> {
        val aiItems = foodAIService.getNutritionPer100g(
            names.map { AIFoodItem(food = it) }
        )

        return aiItems.map { ai ->
            FoodEntity(
                name = ai.name,
                caloriesPer100g = ai.caloriesPer100g,
                proteinPer100g = ai.proteinPer100g,
                carbsPer100g = ai.carbsPer100g,
                fatsPer100g = ai.fatsPer100g,
                source = FoodSource.ai
            )
        }
    }
}
