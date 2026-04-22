package com.shverma.kinetic.ui.fuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.local.entity.MealEntity
import com.shverma.kinetic.data.model.MealItem
import com.shverma.kinetic.data.model.toMealEntity
import com.shverma.kinetic.data.repository.MealRepository
import com.shverma.kinetic.utils.formatMacroGramsOneDecimal
import com.shverma.kinetic.utils.toIsoDateString
import com.shverma.kinetic.utils.toTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LogMealsViewModel @Inject constructor(
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LogMealsState(items = listOf(MealItemData("", ""))))
    val state: StateFlow<LogMealsState> = _state.asStateFlow()

    fun onEvent(event: LogMealsEvents) {
        when (event) {
            is LogMealsEvents.UpdateItemAmount -> updateItemAmount(event.index, event.amount)
            is LogMealsEvents.UpdateItemName -> updateItemName(event.index, event.name)
            is LogMealsEvents.DeleteItem -> deleteItem(event.index)
            is LogMealsEvents.AddItem -> addItem()
            is LogMealsEvents.SaveMeal -> saveMeal()
            is LogMealsEvents.CalculateMacros -> calculateMacros()
        }
    }

    private fun updateItemAmount(index: Int, amount: String) {
        _state.update { currentState ->
            val updatedItems = currentState.items.toMutableList()
            if (index in updatedItems.indices) {
                updatedItems[index] = updatedItems[index].copy(amount = amount)
            }
            currentState.copy(items = updatedItems)
        }
        calculateMacros()
    }

    private fun updateItemName(index: Int, name: String) {
        _state.update { currentState ->
            val updatedItems = currentState.items.toMutableList()
            if (index in updatedItems.indices) {
                updatedItems[index] = updatedItems[index].copy(name = name)
            }
            currentState.copy(items = updatedItems)
        }
        calculateMacros()
    }

    private fun deleteItem(index: Int) {
        _state.update { currentState ->
            val updatedItems = currentState.items.toMutableList()
            if (index in updatedItems.indices) {
                updatedItems.removeAt(index)
            }
            currentState.copy(items = updatedItems)
        }
        calculateMacros()
    }

    private fun addItem() {
        _state.update { it.copy(items = it.items + MealItemData("", "")) }
    }

    private fun saveMeal() {
        val currentState = _state.value
        val mealItems = currentState.items.filter { it.name.isNotBlank() }.map { item ->
            val macroInfo = macroMap[item.name.lowercase().trim()]
            val amount = item.amount.replace("g", "").trim().toDoubleOrNull() ?: 0.0
            val multiplier = amount / 100.0

            MealItem(
                food = item.name,
                quantity = item.amount,
                calories = (macroInfo?.calories ?: 0.0) * multiplier,
                proteinG = (macroInfo?.protein ?: 0.0) * multiplier,
                carbsG = (macroInfo?.carbs ?: 0.0) * multiplier,
                fatsG = (macroInfo?.fats ?: 0.0) * multiplier
            )
        }

        if (mealItems.isEmpty()) return

        val now = Date()

        val meal = com.shverma.kinetic.data.model.Meal(
            name = "Meal",
            time = now.toTimeString(),
            items = mealItems
        )

        val mealEntity = meal.toMealEntity(now.toIsoDateString())

        viewModelScope.launch {
            mealRepository.insertMeal(mealEntity)
        }
    }

    private fun calculateMacros() {
        val currentItems = _state.value.items
        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFats = 0.0

        currentItems.forEach { item ->
            val amount = item.amount.replace("g", "").trim().toDoubleOrNull() ?: 0.0
            val macroInfo = macroMap[item.name.lowercase().trim()]
            
            if (macroInfo != null) {
                val multiplier = amount / 100.0
                totalCalories += macroInfo.calories * multiplier
                totalProtein += macroInfo.protein * multiplier
                totalCarbs += macroInfo.carbs * multiplier
                totalFats += macroInfo.fats * multiplier
            }
        }

        val totalMacros = totalProtein + totalCarbs + totalFats
        val proteinRatio = if (totalMacros > 0) (totalProtein / totalMacros).toFloat() else 0f

        _state.update {
            it.copy(
                totalCalories = totalCalories.formatMacroGramsOneDecimal().replace("g", ""),
                totalProtein = totalProtein.formatMacroGramsOneDecimal().replace("g", ""),
                totalCarbs = totalCarbs.formatMacroGramsOneDecimal().replace("g", ""),
                totalFats = totalFats.formatMacroGramsOneDecimal().replace("g", ""),
                proteinRatio = proteinRatio
            )
        }
    }

    private val macroMap = mapOf(
        "rajma" to MacroInfo(9.0, 20.0, 0.5, 120.0),
        "chana" to MacroInfo(8.0, 27.0, 2.5, 160.0),
        "rice" to MacroInfo(2.5, 28.0, 0.3, 130.0),
        "paneer" to MacroInfo(18.0, 3.0, 20.0, 265.0),
        "oats" to MacroInfo(13.0, 67.0, 7.0, 389.0),
        "soya chunks" to MacroInfo(52.0, 33.0, 0.5, 345.0)
    )

    private data class MacroInfo(val protein: Double, val carbs: Double, val fats: Double, val calories: Double)
}

sealed class LogMealsEvents {
    data class UpdateItemAmount(val index: Int, val amount: String) : LogMealsEvents()
    data class UpdateItemName(val index: Int, val name: String) : LogMealsEvents()
    data class DeleteItem(val index: Int) : LogMealsEvents()
    data object AddItem : LogMealsEvents()
    data object SaveMeal : LogMealsEvents()
    data object CalculateMacros : LogMealsEvents()
}

data class LogMealsState(
    val items: List<MealItemData> = emptyList(),
    val totalCalories: String = "0",
    val totalProtein: String = "0",
    val totalCarbs: String = "0",
    val totalFats: String = "0",
    val proteinRatio: Float = 0f
)

data class MealItemData(val amount: String, val name: String)
