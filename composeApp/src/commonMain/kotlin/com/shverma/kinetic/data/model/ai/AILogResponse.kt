package com.shverma.kinetic.data.model.ai

import kotlinx.serialization.Serializable


@Serializable
data class AILogResponse(
    val type: String = "food_log",
    val entries: List<AILogEntry>,
    val confidence: Double = 0.0
)

@Serializable
data class AILogEntry(
    val mealType: String,
    val items: List<AIFoodItem>
)

val AILogEntry.displayMealType: String
    get() = if (mealType.isBlank()) getMealTypeByTime() else mealType

fun getMealTypeByTime(): String {
    val hour = currentHour()
    return when (hour) {
        in 5..10 -> "Breakfast"
        in 11..15 -> "Lunch"
        in 16..22 -> "Dinner"
        else -> "Snack"
    }
}

expect fun currentHour(): Int
