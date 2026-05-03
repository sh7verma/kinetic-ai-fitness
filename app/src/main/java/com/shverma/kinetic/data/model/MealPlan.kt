package com.shverma.kinetic.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MealPlanResponse(
    val type: String = "meal_plan",
    @SerialName("meal_plan")
    val mealPlan: MealPlan
)

@Serializable
data class SingleLogResponse(
    val type: String = "single_log",
    val entry: LogEntryDetails
)

@Serializable
data class LogEntryDetails(
    val food: String,
    val quantity: String,
    @SerialName("meal_time")
    val mealTime: String,
    val calories: Double,
    @SerialName("protein_g")
    val proteinG: Double,
    @SerialName("carbs_g")
    val carbsG: Double,
    @SerialName("fats_g")
    val fatsG: Double,
    val confidence: Double,
    val note: String
)

@Serializable
data class MultiLogResponse(
    val type: String = "multi_log",
    @SerialName("parsed_from")
    val parsedFrom: String,
    val entries: List<LogEntry>,
    @SerialName("total_calories")
    val totalCalories: Double,
    val confidence: Double
)

@Serializable
data class LogEntry(
    @SerialName("meal_time")
    val mealTime: String,
    val items: List<MealItem>
) {
    val totalCalories: Double get() = items.sumOf { it.calories }
    val totalProtein: Double get() = items.sumOf { it.proteinG }
    val totalCarbs: Double get() = items.sumOf { it.carbsG }
    val totalFats: Double get() = items.sumOf { it.fatsG }
}

@Serializable
data class MealPlan(
    val date: String = "",
    val goal: String = "", // muscle_gain|fat_loss|maintenance|performance
    @SerialName("total_calories")
    val totalCalories: Double = 0.0,
    val macros: Macros = Macros(),
    val meals: List<Meal> = emptyList()
)

@Serializable
data class Macros(
    @SerialName("protein_g")
    val proteinG: Double = 0.0,
    @SerialName("carbs_g")
    val carbsG: Double = 0.0,
    @SerialName("fats_g")
    val fatsG: Double = 0.0
)

@Serializable
data class Meal(
    val name: String = "", // Breakfast|Lunch|Dinner|Pre-workout|Post-workout|Snack
    val time: String = "",
    val items: List<MealItem> = emptyList()
)

fun Meal.toMealEntity(date: String): com.shverma.kinetic.data.local.entity.MealEntity {
    return com.shverma.kinetic.data.local.entity.MealEntity(
        name = this.name,
        time = this.time,
        date = date,
        items = this.items,
        totalCalories = this.items.sumOf { it.calories },
        totalProtein = this.items.sumOf { it.proteinG },
        totalCarbs = this.items.sumOf { it.carbsG },
        totalFats = this.items.sumOf { it.fatsG }
    )
}

@Serializable
data class MealItem(
    val food: String = "",
    val quantity: String = "",
    val calories: Double = 0.0,
    @SerialName("protein_g")
    val proteinG: Double = 0.0,
    @SerialName("carbs_g")
    val carbsG: Double = 0.0,
    @SerialName("fats_g")
    val fatsG: Double = 0.0
)
