package com.shverma.kinetic.ui.fuel

import kotlinx.coroutines.flow.StateFlow

interface FuelActions {
    val state: StateFlow<FuelState>

    fun repeatMeal(meal: LoggedMealGroup)
}

data class FuelState(
    val caloriesValue: String = "",
    val aiExplanation: String = "",
    val caloriesProgress: Float = 0f,
    val caloriesEaten: String = "",
    val caloriesTarget: String = "",
    val proteinValue: String = "",
    val proteinPercent: String = "",
    val carbsValue: String = "",
    val carbsPercent: String = "",
    val fatsValue: String = "",
    val fatsPercent: String = "",
    val weeklyTrend: List<Pair<String, Double>> = emptyList(),
    val todayIndex: Int = 0,
    val todaysMeals: List<LoggedMealGroup> = emptyList(),
    val quickRepeats: List<LoggedMealGroup> = emptyList(),
)

data class LoggedMealGroup(
    val displayName: String,
    val mealType: String,
    val time: String,
    val timestamp: Long,
    val totalCalories: Double,
    val items: List<LoggedMealItem>,
)

data class LoggedMealItem(
    val foodId: Int,
    val grams: Double,
    val mealType: String,
)
