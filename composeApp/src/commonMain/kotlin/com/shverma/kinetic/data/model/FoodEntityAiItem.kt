package com.shverma.kinetic.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FoodEntityAiResponse(
    val type: String = "food_entity_batch",
    val items: List<FoodEntityAiItem>
)

@Serializable
data class FoodEntityAiItem(
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatsPer100g: Double
)
