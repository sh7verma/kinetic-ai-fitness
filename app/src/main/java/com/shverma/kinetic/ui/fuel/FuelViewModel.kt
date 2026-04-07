package com.shverma.kinetic.ui.fuel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FuelViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(FuelState())
    val state: StateFlow<FuelState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: FuelEvents) {
        when (event) {
            is FuelEvents.LoadData -> loadData()
            is FuelEvents.AddMeal -> addMeal()
        }
    }

    private fun addMeal() {
        val newMeal = FuelItem("New Meal", "SNACK", "Now", 250)
        _state.value = _state.value.copy(
            recentFuelList = listOf(newMeal) + _state.value.recentFuelList
        )
    }

    private fun loadData() {
        _state.value = _state.value.copy(
            caloriesValue = "1,842",
            caloriesProgress = 0.65f,
            caloriesEaten = "1,240",
            caloriesBurned = "450",
            waterValue = "2.4L",
            fastingValue = "16:8",
            proteinValue = "142g",
            proteinPercent = "72%",
            carbsValue = "210g",
            carbsPercent = "54%",
            fatsValue = "64g",
            fatsPercent = "38%",
            weeklyTrend = listOf(
                "M" to 1200,
                "T" to 1800,
                "W" to 2200,
                "T" to 1500,
                "F" to 2600,
                "S" to 2000,
                "S" to 1000
            ),
            recentFuelList = dummyFuelList
        )
    }
}

sealed class FuelEvents {
    data object LoadData : FuelEvents()
    data object AddMeal : FuelEvents()
}

data class FuelState(
    val caloriesValue: String = "",
    val caloriesProgress: Float = 0f,
    val caloriesEaten: String = "",
    val caloriesBurned: String = "",
    val waterValue: String = "",
    val fastingValue: String = "",
    val proteinValue: String = "",
    val proteinPercent: String = "",
    val carbsValue: String = "",
    val carbsPercent: String = "",
    val fatsValue: String = "",
    val fatsPercent: String = "",
    val weeklyTrend: List<Pair<String, Int>> = emptyList(),
    val recentFuelList: List<FuelItem> = emptyList()
)

data class FuelItem(val title: String, val category: String, val time: String, val calories: Int)

val dummyFuelList = listOf(
    FuelItem("Grilled Chicken Salad", "LUNCH", "12:45 PM", 450),
    FuelItem("Protein Shake", "SNACK", "10:30 AM", 180),
    FuelItem("Oatmeal with Berries", "BREAKFAST", "08:15 AM", 320)
)
