package com.shverma.kinetic.ui.fuel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.repository.MealRepository
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.data.model.Meal
import com.shverma.kinetic.data.model.MealPlan
import com.shverma.kinetic.data.model.toMealEntity
import kotlinx.coroutines.launch
import com.shverma.kinetic.data.model.DietaryGoal
import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.utils.addDays
import com.shverma.kinetic.utils.calculateProgress
import com.shverma.kinetic.utils.formatCalories
import com.shverma.kinetic.utils.formatMacroGrams
import com.shverma.kinetic.utils.formatPercentage
import com.shverma.kinetic.utils.subtractDays
import com.shverma.kinetic.utils.toDayOfWeekShort
import com.shverma.kinetic.utils.toIsoDateString
import com.shverma.kinetic.utils.toTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class FuelViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val userProfileRepository: UserProfileRepository,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {

    private val _state = MutableStateFlow(FuelState())
    val state: StateFlow<FuelState> = _state.asStateFlow()

    init {
        loadInitialData()
        observeData()
    }

    private fun observeData() {
        val today = Date().toIsoDateString()
        val sevenDaysAgo = Date().subtractDays(6).toIsoDateString()

        combine(
            mealRepository.getMealsByDate(today),
            mealRepository.getMealsFromDate(sevenDaysAgo),
            userProfileRepository.getUserProfileData(),
            dataStoreHelper.mealPlan
        ) { meals, lastSevenDaysMeals, userProfile, mealPlan ->
            Log.d("FuelViewModel", "Meals: ${meals.size}, UserProfile: ${userProfile != null}, MealPlan: ${mealPlan != null}")
            
            val totalEaten = meals.sumOf { it.totalCalories }
            val targetCalories = mealPlan?.totalCalories ?: userProfile?.calculateTargetCalories() ?: 2000.0
            val remaining = targetCalories - totalEaten

            val totalProtein = meals.sumOf { it.totalProtein }
            val totalCarbs = meals.sumOf { it.totalCarbs }
            val totalFats = meals.sumOf { it.totalFats }

            // Calculate targets
            var targetProtein = 0.0
            var targetCarbs = 0.0
            var targetFats = 0.0

            if (mealPlan != null) {
                targetProtein = mealPlan.macros.proteinG
                targetCarbs = mealPlan.macros.carbsG
                targetFats = mealPlan.macros.fatsG
            } else {
                // Default macro split based on goal
                val goal = userProfile?.dietaryGoal ?: DietaryGoal.MUSCLE_GAIN
                when (goal) {
                    DietaryGoal.MUSCLE_GAIN -> {
                        // 30% Protein, 45% Carbs, 25% Fats
                        targetProtein = (targetCalories * 0.30) / 4
                        targetCarbs = (targetCalories * 0.45) / 4
                        targetFats = (targetCalories * 0.25) / 9
                    }
                    DietaryGoal.FAT_LOSS -> {
                        // 40% Protein, 35% Carbs, 25% Fats
                        targetProtein = (targetCalories * 0.40) / 4
                        targetCarbs = (targetCalories * 0.35) / 4
                        targetFats = (targetCalories * 0.25) / 9
                    }
                }
            }

            // Calculate weekly trend
            val trendMap = lastSevenDaysMeals.groupBy { it.date }
                .mapValues { entry -> entry.value.sumOf { it.totalCalories } }

            val weeklyTrend = mutableListOf<Pair<String, Double>>()
            var tempDate = Date().subtractDays(6)
            for (i in 0..6) {
                val dateStr = tempDate.toIsoDateString()
                val dayStr = tempDate.toDayOfWeekShort().take(1).uppercase()
                weeklyTrend.add(dayStr to (trendMap[dateStr] ?: 0.0))
                tempDate = tempDate.addDays(1)
            }

            _state.update { currentState ->
                currentState.copy(
                    caloriesValue = remaining.formatCalories(),
                    caloriesEaten = totalEaten.formatCalories(),
                    caloriesProgress = calculateProgress(totalEaten, targetCalories),
                    caloriesTarget = targetCalories.formatCalories(),
                    proteinValue = totalProtein.formatMacroGrams(),
                    proteinPercent = if (targetProtein > 0) (totalProtein / targetProtein * 100).formatPercentage() else "0%",
                    carbsValue = totalCarbs.formatMacroGrams(),
                    carbsPercent = if (targetCarbs > 0) (totalCarbs / targetCarbs * 100).formatPercentage() else "0%",
                    fatsValue = totalFats.formatMacroGrams(),
                    fatsPercent = if (targetFats > 0) (totalFats / targetFats * 100).formatPercentage() else "0%",
                    mealPlan = mealPlan,
                    loggedMealNames = meals.map { it.name }.toSet(),
                    weeklyTrend = weeklyTrend
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: FuelEvents) {
        when (event) {
            is FuelEvents.LoadData -> loadInitialData()
            is FuelEvents.AddMeal -> addMeal()
            is FuelEvents.LogPlannedMeal -> logPlannedMeal(event.meal)
        }
    }

    private fun logPlannedMeal(meal: Meal) {
        viewModelScope.launch {
            val today = Date().toIsoDateString()
            val now = Date().toTimeString()
            mealRepository.insertMeal(meal.copy(time = now).toMealEntity(today))
        }
    }

    private fun addMeal() {
        // Not implemented in repository yet, but keeping original structure
    }

    private fun loadInitialData() {
        // Data is now observed from repository
    }
}

sealed class FuelEvents {
    data object LoadData : FuelEvents()
    data object AddMeal : FuelEvents()
    data class LogPlannedMeal(val meal: Meal) : FuelEvents()
}

data class FuelState(
    val caloriesValue: String = "",
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
    val mealPlan: MealPlan? = null,
    val loggedMealNames: Set<String> = emptySet()
)

data class FuelItem(val title: String, val category: String, val time: String, val calories: Double)

