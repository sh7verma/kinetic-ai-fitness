package com.shverma.kinetic.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.model.ai.AIDietPlan
import com.shverma.kinetic.data.model.ai.AIDietPlanResponse
import com.shverma.kinetic.data.model.ai.safeGrams
import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.data.repository.DietAIRepository
import com.shverma.kinetic.data.repository.FoodResolver
import com.shverma.kinetic.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CreateDietPlanViewModel @Inject constructor(
    private val dietAIRepository: DietAIRepository,
    private val dataStoreHelper: DataStoreHelper,
    private val foodResolver: FoodResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow<DietPlanUiState>(DietPlanUiState.Loading)
    val uiState: StateFlow<DietPlanUiState> = _uiState.asStateFlow()

    private var rawPlan: AIDietPlan? = null

    init {
        loadExistingPlan()
    }

    private fun loadExistingPlan() {
        viewModelScope.launch {
            dataStoreHelper.dietPlan.collectLatest { plan ->
                if (plan != null) {
                    rawPlan = plan
                    _uiState.value = DietPlanUiState.Success(plan.toUiModelSimple(), isSaved = true)
                    // Then enrich it
                    plan.toUiModelFlow(foodResolver).collectLatest { enrichedPlan ->
                        _uiState.value = DietPlanUiState.Success(enrichedPlan, isSaved = true)
                    }
                } else {
                    createDietPlan()
                }
            }
        }
    }

    fun createDietPlan(query: String = "Create a healthy meal plan for me") {
        viewModelScope.launch {
            _uiState.value = DietPlanUiState.Loading
            when (val result = dietAIRepository.createDietPlan(query)) {
                is Resource.Success -> {
                    val data = result.data
                    if (data != null) {
                        rawPlan = data.diet_plan
                        _uiState.value = DietPlanUiState.Success(data.diet_plan.toUiModelSimple())
                        // Then enrich
                        data.toUiModelFlow(foodResolver).collectLatest { enrichedPlan ->
                            _uiState.value = DietPlanUiState.Success(enrichedPlan)
                        }
                    } else {
                        _uiState.value = DietPlanUiState.Error("Failed to generate plan")
                    }
                }

                is Resource.Error -> {
                    _uiState.value = DietPlanUiState.Error(result.message ?: "Unknown error")
                }

                else -> {
                    _uiState.value = DietPlanUiState.Error("Unknown error")
                }
            }
        }
    }

    fun updateSummary(calories: String, protein: String, carbs: String, fat: String) {
        _uiState.update { state ->
            if (state is DietPlanUiState.Success) {
                state.copy(
                    plan = state.plan.copy(
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fat = fat
                    )
                )
            } else state
        }
    }

    fun updateMealName(mealId: String, name: String) {
        _uiState.update { state ->
            if (state is DietPlanUiState.Success) {
                val updatedMeals = state.plan.meals.map {
                    if (it.id == mealId) it.copy(name = name) else it
                }
                state.copy(plan = state.plan.copy(meals = updatedMeals))
            } else state
        }
    }

    fun addFoodItem(mealId: String) {
        _uiState.update { state ->
            if (state is DietPlanUiState.Success) {
                val updatedMeals = state.plan.meals.map { meal ->
                    if (meal.id == mealId) {
                        meal.copy(
                            items = meal.items + FoodItemUiModel(
                                id = UUID.randomUUID().toString()
                            )
                        )
                    } else meal
                }
                state.copy(plan = state.plan.copy(meals = updatedMeals))
            } else state
        }
    }

    fun updateFoodItem(
        mealId: String,
        foodId: String,
        name: String,
        quantity: String,
        unit: String
    ) {
        _uiState.update { state ->
            if (state is DietPlanUiState.Success) {
                val updatedMeals = state.plan.meals.map { meal ->
                    if (meal.id == mealId) {
                        val updatedItems = meal.items.map { item ->
                            if (item.id == foodId) {
                                item.copy(name = name, quantity = quantity, unit = unit)
                            } else item
                        }
                        meal.copy(items = updatedItems)
                    } else meal
                }
                state.copy(plan = state.plan.copy(meals = updatedMeals))
            } else state
        }
    }

    fun removeFoodItem(mealId: String, foodId: String) {
        _uiState.update { state ->
            if (state is DietPlanUiState.Success) {
                val updatedMeals = state.plan.meals.map { meal ->
                    if (meal.id == mealId) {
                        meal.copy(items = meal.items.filter { it.id != foodId })
                    } else meal
                }
                state.copy(plan = state.plan.copy(meals = updatedMeals))
            } else state
        }
    }

    fun savePlan() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is DietPlanUiState.Success) {
                rawPlan?.let {
                    dataStoreHelper.saveDietPlan(it)
                    _uiState.value = state.copy(isSaved = true)
                }
            }
        }
    }

    private fun AIDietPlan.toUiModelSimple(): DietPlanUiModel {
        return DietPlanUiModel(
            calories = "2000",
            protein = "150",
            carbs = "200",
            fat = "60",
            meals = this.meals.map { aiMeal ->
                MealUiModel(
                    id = UUID.randomUUID().toString(),
                    name = aiMeal.name,
                    items = aiMeal.items.map { aiFood ->
                        FoodItemUiModel(
                            id = UUID.randomUUID().toString(),
                            name = aiFood.food,
                            quantity = aiFood.grams ?: "100",
                            unit = "g"
                        )
                    }
                )
            }
        )
    }

    private fun AIDietPlan.toUiModelFlow(foodResolver: FoodResolver): Flow<DietPlanUiModel> {
        val allItems = meals.flatMap { it.items }
        return foodResolver.resolveFoods(allItems).map { foodMap ->
            val uiMeals = this.meals.map { aiMeal ->
                val items = aiMeal.items.map { aiFood ->
                    val resolvedName = foodResolver.resolve(aiFood.food)
                    val food = foodMap[resolvedName]

                    val grams = safeGrams(aiFood.grams ?: "100")
                    val factor = grams / 100.0

                    FoodItemUiModel(
                        id = UUID.randomUUID().toString(),
                        name = food?.name ?: aiFood.food,
                        quantity = grams.roundToInt().toString(),
                        unit = "g",
                        calories = food?.let { (it.caloriesPer100g * factor).roundToInt() },
                        protein = food?.let { (it.proteinPer100g * factor).roundToInt() },
                        carbs = food?.let { (it.carbsPer100g * factor).roundToInt() },
                        fat = food?.let { (it.fatsPer100g * factor).roundToInt() }
                    )
                }
                MealUiModel(
                    id = UUID.randomUUID().toString(),
                    name = aiMeal.name,
                    items = items
                )
            }

            val totalCalories = uiMeals.sumOf { it.items.sumOf { item -> item.calories ?: 0 } }
            val totalProtein = uiMeals.sumOf { it.items.sumOf { item -> item.protein ?: 0 } }
            val totalCarbs = uiMeals.sumOf { it.items.sumOf { item -> item.carbs ?: 0 } }
            val totalFat = uiMeals.sumOf { it.items.sumOf { item -> item.fat ?: 0 } }

            DietPlanUiModel(
                calories = totalCalories.toString(),
                protein = totalProtein.toString(),
                carbs = totalCarbs.toString(),
                fat = totalFat.toString(),
                meals = uiMeals
            )
        }
    }

    private fun AIDietPlanResponse.toUiModelFlow(foodResolver: FoodResolver): Flow<DietPlanUiModel> {
        return this.diet_plan.toUiModelFlow(foodResolver)
    }

    private suspend fun AIDietPlan.toUiModel(foodResolver: FoodResolver): DietPlanUiModel {
        // This remains for backward compatibility or one-time use if needed, 
        // but it's better to use toUiModelFlow
        return toUiModelFlow(foodResolver).last()
    }
}

sealed class DietPlanUiState {
    object Loading : DietPlanUiState()
    data class Success(val plan: DietPlanUiModel, val isSaved: Boolean = false) : DietPlanUiState()
    data class Error(val message: String) : DietPlanUiState()
}

data class DietPlanUiModel(
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val meals: List<MealUiModel> = emptyList()
)

data class MealUiModel(
    val id: String,
    val name: String,
    val items: List<FoodItemUiModel>
)

data class FoodItemUiModel(
    val id: String,
    val name: String = "",
    val quantity: String = "",
    val unit: String = "g",
    val calories: Int? = null,
    val protein: Int? = null,
    val carbs: Int? = null,
    val fat: Int? = null
)
