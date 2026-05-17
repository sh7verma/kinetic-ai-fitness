package com.shverma.kinetic.ui.fuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.local.entity.FoodEntity
import com.shverma.kinetic.data.local.entity.FoodLogEntity
import com.shverma.kinetic.data.local.entity.FoodLogWithFood
import com.shverma.kinetic.data.model.ai.TargetCaloriesData
import com.shverma.kinetic.data.repository.FoodResolver
import com.shverma.kinetic.data.repository.MacrosCalculator
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.utils.formatCalories
import com.shverma.kinetic.utils.formatPercentage
import com.shverma.kinetic.utils.toTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class FuelViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val foodLogDao: FoodLogDao,
    private val foodDao: FoodDao,
    private val foodResolver: FoodResolver
) : ViewModel() {

    private val startOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val endOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    private val startOfWeek = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val state: StateFlow<FuelState> = combine(
        userProfileRepository.getUserProfileData(),
        foodLogDao.getFoodLogsWithFoodInRange(startOfDay, endOfDay),
        foodLogDao.getFoodLogsWithFoodInRange(startOfWeek, endOfDay)
    ) { profile, dailyLogs, weeklyLogs ->
        if (profile == null) return@combine FuelState()

        val target = profile.targetCaloriesData ?: MacrosCalculator.fallback(profile)

        // 1. Calculate Daily Totals
        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFats = 0.0
        val items = mutableListOf<FuelItem>()

        dailyLogs.forEach { logWithFood ->
            val log = logWithFood.log
            val food = logWithFood.food
            val factor = log.grams / 100.0
            val calories = food.caloriesPer100g * factor
            val protein = food.proteinPer100g * factor
            val carbs = food.carbsPer100g * factor
            val fats = food.fatsPer100g * factor

            totalCalories += calories
            totalProtein += protein
            totalCarbs += carbs
            totalFats += fats

            items.add(
                FuelItem(
                    title = food.name,
                    category = log.mealType,
                    time = Date(log.timestamp ?: log.createdAt).toTimeString(),
                    calories = calories
                )
            )
        }

        // 2. Calculate Weekly Trend
        val dayLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val caloriesPerDay = DoubleArray(7)

        val calendar = Calendar.getInstance()
        weeklyLogs.forEach { logWithFood ->
            val log = logWithFood.log
            val food = logWithFood.food
            val calories = food.caloriesPer100g * (log.grams / 100.0)
            calendar.timeInMillis = log.timestamp ?: log.createdAt
            // Adjust to 0-indexed Monday
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val index = when (dayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            caloriesPerDay[index] += calories
        }

        val weeklyTrend = dayLabels.mapIndexed { index, label ->
            label to caloriesPerDay[index]
        }

        val remaining = (target.targetCalories - totalCalories).coerceAtLeast(0.0)

        FuelState(
            caloriesValue = remaining.formatCalories(),
            aiExplanation = target.explanation ?: "Based on your profile",
            caloriesProgress = (totalCalories / target.targetCalories).toFloat().coerceIn(0f, 1f),
            caloriesEaten = totalCalories.formatCalories(),
            caloriesTarget = target.targetCalories.formatCalories(),
            proteinValue = String.format("%.0f/%.0fg", totalProtein, target.proteinG),
            proteinPercent = ((totalProtein / target.proteinG) * 100).formatPercentage(),
            carbsValue = String.format("%.0f/%.0fg", totalCarbs, target.carbsG),
            carbsPercent = ((totalCarbs / target.carbsG) * 100).formatPercentage(),
            fatsValue = String.format("%.0f/%.0fg", totalFats, target.fatsG),
            fatsPercent = ((totalFats / target.fatsG) * 100).formatPercentage(),
            loggedMealNames = dailyLogs.map { it.log.mealType }.toSet(),
            items = items,
            weeklyTrend = weeklyTrend
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FuelState()
    )
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
    val loggedMealNames: Set<String> = emptySet(),
    val items: List<FuelItem> = emptyList()
)

data class FuelItem(val title: String, val category: String, val time: String, val calories: Double)

