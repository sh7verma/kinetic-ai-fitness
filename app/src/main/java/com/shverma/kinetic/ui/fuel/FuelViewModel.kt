package com.shverma.kinetic.ui.fuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.local.entity.FoodLogEntity
import com.shverma.kinetic.data.local.entity.FoodLogWithFood
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

        dailyLogs.forEach { logWithFood ->
            val log = logWithFood.log
            val food = logWithFood.food
            val factor = log.grams / 100.0
            totalCalories += food.caloriesPer100g * factor
            totalProtein += food.proteinPer100g * factor
            totalCarbs += food.carbsPer100g * factor
            totalFats += food.fatsPer100g * factor
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

        // 3. Today's meal history — grouped chronologically, oldest first
        val todaysMeals = groupIntoMeals(dailyLogs).sortedBy { it.timestamp }

        // 4. Quick-repeat — most recent distinct meals from the last 7 days
        val quickRepeats = groupIntoMeals(weeklyLogs)
            .sortedByDescending { it.timestamp }
            .distinctBy { it.displayName }
            .take(2)

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
            todaysMeals = todaysMeals,
            quickRepeats = quickRepeats,
            weeklyTrend = weeklyTrend
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FuelState()
    )

    /** Re-logs a previously logged meal without calling the AI again. */
    fun repeatMeal(meal: LoggedMealGroup) {
        viewModelScope.launch {
            meal.items.forEach { item ->
                foodLogDao.insert(
                    FoodLogEntity(
                        foodId = item.log.foodId,
                        grams = item.log.grams,
                        timestamp = System.currentTimeMillis(),
                        mealType = item.log.mealType
                    )
                )
            }
        }
    }

    /**
     * Groups individual logged food items back into the "meal" the user logged
     * together — items saved by the same AIChatViewModel.saveMeal() call share a
     * mealType and land within the same minute, since that's a single fast DB
     * write loop, not a slow multi-step process.
     */
    private fun groupIntoMeals(logs: List<FoodLogWithFood>): List<LoggedMealGroup> {
        val groups = LinkedHashMap<String, MutableList<FoodLogWithFood>>()
        logs.forEach { item ->
            val ts = item.log.timestamp ?: item.log.createdAt
            val minuteBucket = ts / 60_000
            val key = "${item.log.mealType}_$minuteBucket"
            groups.getOrPut(key) { mutableListOf() }.add(item)
        }
        return groups.values.map { groupItems ->
            val firstLog = groupItems.first().log
            val ts = firstLog.timestamp ?: firstLog.createdAt
            LoggedMealGroup(
                displayName = groupItems.joinToString(", ") { it.food.name },
                mealType = firstLog.mealType,
                time = Date(ts).toTimeString(),
                timestamp = ts,
                totalCalories = groupItems.sumOf { it.food.caloriesPer100g * (it.log.grams / 100.0) },
                items = groupItems
            )
        }
    }
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
    val todaysMeals: List<LoggedMealGroup> = emptyList(),
    val quickRepeats: List<LoggedMealGroup> = emptyList()
)

data class LoggedMealGroup(
    val displayName: String,
    val mealType: String,
    val time: String,
    val timestamp: Long,
    val totalCalories: Double,
    val items: List<FoodLogWithFood>
)
