package com.shverma.kinetic.ui.fuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.local.entity.MealEntity
import com.shverma.kinetic.data.model.MealPlan
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.model.DietaryGoal
import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.data.repository.MealRepository
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.utils.toIsoDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.util.Date
import javax.inject.Inject

data class TodayMealPlanState(
    val mealPlan: MealPlan? = null,
    val todayMeals: List<MealEntity> = emptyList(),
    val totalCalories: Double = 0.0,
    val targetCalories: Double = 2000.0,
    val remainingCalories: Double = 2000.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFats: Double = 0.0,
    val targetProtein: Double = 0.0,
    val targetCarbs: Double = 0.0,
    val targetFats: Double = 0.0,
    val isLoading: Boolean = true,
    val loggedMealNames: Set<String> = emptySet()
)

@HiltViewModel
class TodayMealPlanViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val userProfileRepository: UserProfileRepository,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {

    private val _state = MutableStateFlow(TodayMealPlanState())
    val state: StateFlow<TodayMealPlanState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val today = Date().toIsoDateString()

        combine(
            dataStoreHelper.mealPlan,
            mealRepository.getMealsByDate(today),
            userProfileRepository.getUserProfileData()
        ) { plan, meals, userProfile ->
            val totalEaten = meals.sumOf { it.totalCalories }
            val target = plan?.totalCalories ?: userProfile?.calculateTargetCalories() ?: 2000.0

            val totalProtein = meals.sumOf { it.totalProtein }
            val totalCarbs = meals.sumOf { it.totalCarbs }
            val totalFats = meals.sumOf { it.totalFats }

            var targetProtein = 0.0
            var targetCarbs = 0.0
            var targetFats = 0.0

            if (plan != null) {
                targetProtein = plan.macros.proteinG
                targetCarbs = plan.macros.carbsG
                targetFats = plan.macros.fatsG
            } else {
                val goal = userProfile?.dietaryGoal ?: DietaryGoal.MUSCLE_GAIN
                when (goal) {
                    DietaryGoal.MUSCLE_GAIN -> {
                        targetProtein = (target * 0.30) / 4
                        targetCarbs = (target * 0.45) / 4
                        targetFats = (target * 0.25) / 9
                    }
                    DietaryGoal.FAT_LOSS -> {
                        targetProtein = (target * 0.40) / 4
                        targetCarbs = (target * 0.35) / 4
                        targetFats = (target * 0.25) / 9
                    }
                }
            }
            
            TodayMealPlanState(
                mealPlan = plan,
                todayMeals = meals,
                totalCalories = totalEaten,
                targetCalories = target,
                remainingCalories = target - totalEaten,
                totalProtein = totalProtein,
                totalCarbs = totalCarbs,
                totalFats = totalFats,
                targetProtein = targetProtein,
                targetCarbs = targetCarbs,
                targetFats = targetFats,
                isLoading = false,
                loggedMealNames = meals.map { it.name }.toSet()
            )
        }.onEach { newState ->
            _state.value = newState
        }.launchIn(viewModelScope)
    }
}
