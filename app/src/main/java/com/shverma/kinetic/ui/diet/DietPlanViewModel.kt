package com.shverma.kinetic.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.local.entity.FoodLogEntity
import com.shverma.kinetic.data.local.entity.FoodLogWithFood
import com.shverma.kinetic.data.local.entity.toMacros
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.model.ai.AIDietPlan
import com.shverma.kinetic.data.model.ai.toGrams
import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.data.repository.FoodResolver
import com.shverma.kinetic.ui.aichat.UIFoodItem
import com.shverma.kinetic.ui.aichat.UIMeal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DietPlanUi(
    val goal: String,
    val meals: List<UIMeal>
)

@HiltViewModel
class DietPlanViewModel @Inject constructor(
    private val dataStoreHelper: DataStoreHelper,
    private val foodLogDao: FoodLogDao,
    private val foodDao: FoodDao,
    private val foodResolver: FoodResolver
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis)
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()
    val userProfile: Flow<UserProfileData?> = dataStoreHelper.userProfileData

    @OptIn(ExperimentalCoroutinesApi::class)
    val dietPlan: Flow<DietPlanUi?> = dataStoreHelper.dietPlan.flatMapLatest { plan ->
        if (plan == null) return@flatMapLatest flowOf(null)

        // Collect all items from all meals
        val allItems = plan.meals.flatMap { it.items }

        // Transform resolveFoods flow into DietPlanUi flow
        foodResolver.resolveFoods(allItems).map { resolvedMap ->
            val uiMeals = plan.meals.map { aiMeal ->
                val uiFoodItems = aiMeal.items.map { item ->
                    val normalizedName = foodResolver.resolve(item.food)
                    val food = resolvedMap[normalizedName]
                    val grams = item.grams?.toGrams() ?: 100.0
                    val factor = grams / 100.0

                    UIFoodItem(
                        name = item.food,
                        grams = grams,
                        calories = (food?.caloriesPer100g ?: 0.0) * factor,
                        protein = (food?.proteinPer100g ?: 0.0) * factor,
                        carbs = (food?.carbsPer100g ?: 0.0) * factor,
                        fats = (food?.fatsPer100g ?: 0.0) * factor
                    )
                }

                UIMeal(
                    mealType = aiMeal.name,
                    items = uiFoodItems,
                    totalCalories = uiFoodItems.sumOf { it.calories },
                    totalProtein = uiFoodItems.sumOf { it.protein },
                    totalCarbs = uiFoodItems.sumOf { it.carbs },
                    totalFats = uiFoodItems.sumOf { it.fats }
                )
            }
            DietPlanUi(goal = plan.goal, meals = uiMeals)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val foodLogs: StateFlow<List<UIMeal>> = _selectedDate.flatMapLatest { date ->
        val startOfDay = date
        val endOfDay = date + 24 * 60 * 60 * 1000 - 1
        foodLogDao.getFoodLogsWithFoodInRange(startOfDay, endOfDay).map { logs ->
            // Group logs by meal type
            val groupedLogs = logs.groupBy { it.log.mealType }
            groupedLogs.map { (mealType, mealLogs) ->
                val uiFoodItems = mealLogs.map { logWithFood ->
                    val log = logWithFood.log
                    val food = logWithFood.food
                    val macros = log.toMacros(food)
                    UIFoodItem(
                        name = food.name,
                        grams = log.grams,
                        calories = macros.calories,
                        protein = macros.protein,
                        carbs = macros.carbs,
                        fats = macros.fats
                    )
                }
                UIMeal(
                    mealType = mealType,
                    items = uiFoodItems,
                    totalCalories = uiFoodItems.sumOf { it.calories },
                    totalProtein = uiFoodItems.sumOf { it.protein },
                    totalCarbs = uiFoodItems.sumOf { it.carbs },
                    totalFats = uiFoodItems.sumOf { it.fats }
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onDateSelected(date: Long) {
        _selectedDate.value = date
    }
}
