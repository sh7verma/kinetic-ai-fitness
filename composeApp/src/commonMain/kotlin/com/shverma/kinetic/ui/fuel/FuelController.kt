package com.shverma.kinetic.ui.fuel

import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.local.entity.FoodLogEntity
import com.shverma.kinetic.data.local.entity.FoodLogWithFood
import com.shverma.kinetic.data.repository.MacrosCalculator
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.utils.currentTimeMillis
import com.shverma.kinetic.utils.formatCalories
import com.shverma.kinetic.utils.formatMacroPair
import com.shverma.kinetic.utils.formatPercentage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FuelController(
    private val userProfileRepository: UserProfileRepository,
    private val foodLogDao: FoodLogDao,
    private val clock: FuelClock,
    private val scope: CoroutineScope,
) : FuelActions {
    private val timeWindow = clock.currentWindow()

    override val state: StateFlow<FuelState> = combine(
        userProfileRepository.getUserProfileData(),
        foodLogDao.getFoodLogsWithFoodInRange(timeWindow.startOfDay, timeWindow.endOfDay),
        foodLogDao.getFoodLogsWithFoodInRange(timeWindow.startOfWeek, timeWindow.endOfDay),
    ) { profile, dailyLogs, weeklyLogs ->
        if (profile == null) return@combine FuelState()

        val target = profile.targetCaloriesData ?: MacrosCalculator.fallback(profile)

        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFats = 0.0

        dailyLogs.forEach { logWithFood ->
            val factor = logWithFood.log.grams / 100.0
            totalCalories += logWithFood.food.caloriesPer100g * factor
            totalProtein += logWithFood.food.proteinPer100g * factor
            totalCarbs += logWithFood.food.carbsPer100g * factor
            totalFats += logWithFood.food.fatsPer100g * factor
        }

        val dayLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val caloriesPerDay = DoubleArray(7)
        weeklyLogs.forEach { logWithFood ->
            val calories = logWithFood.food.caloriesPer100g * (logWithFood.log.grams / 100.0)
            caloriesPerDay[clock.dayIndex(logWithFood.log.timestamp ?: logWithFood.log.createdAt)] += calories
        }

        val weeklyTrend = dayLabels.mapIndexed { index, label -> label to caloriesPerDay[index] }
        val remaining = (target.targetCalories - totalCalories).coerceAtLeast(0.0)

        FuelState(
            caloriesValue = remaining.formatCalories(),
            aiExplanation = target.explanation ?: "Based on your profile",
            caloriesProgress = (totalCalories / target.targetCalories).toFloat().coerceIn(0f, 1f),
            caloriesEaten = totalCalories.formatCalories(),
            caloriesTarget = target.targetCalories.formatCalories(),
            proteinValue = formatMacroPair(totalProtein, target.proteinG),
            proteinPercent = ((totalProtein / target.proteinG) * 100).formatPercentage(),
            carbsValue = formatMacroPair(totalCarbs, target.carbsG),
            carbsPercent = ((totalCarbs / target.carbsG) * 100).formatPercentage(),
            fatsValue = formatMacroPair(totalFats, target.fatsG),
            fatsPercent = ((totalFats / target.fatsG) * 100).formatPercentage(),
            todaysMeals = groupIntoMeals(dailyLogs),
            quickRepeats = groupIntoMeals(weeklyLogs)
                .sortedByDescending { it.timestamp }
                .distinctBy { it.displayName }
                .take(2),
            weeklyTrend = weeklyTrend,
            todayIndex = timeWindow.todayIndex,
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FuelState(),
    )

    override fun repeatMeal(meal: LoggedMealGroup) {
        scope.launch {
            meal.items.forEach { item ->
                foodLogDao.insert(
                    FoodLogEntity(
                        foodId = item.foodId,
                        grams = item.grams,
                        timestamp = currentTimeMillis(),
                        mealType = item.mealType,
                    ),
                )
            }
        }
    }

    private fun groupIntoMeals(logs: List<FoodLogWithFood>): List<LoggedMealGroup> {
        val groups = linkedMapOf<String, MutableList<FoodLogWithFood>>()
        logs.forEach { item ->
            val timestamp = item.log.timestamp ?: item.log.createdAt
            val minuteBucket = timestamp / 60_000
            val key = "${item.log.mealType}_$minuteBucket"
            groups.getOrPut(key) { mutableListOf() }.add(item)
        }

        return groups.values.map { groupItems ->
            val firstLog = groupItems.first().log
            val timestamp = firstLog.timestamp ?: firstLog.createdAt
            LoggedMealGroup(
                displayName = groupItems.joinToString(", ") { it.food.name },
                mealType = firstLog.mealType,
                time = clock.formatTime(timestamp),
                timestamp = timestamp,
                totalCalories = groupItems.sumOf {
                    it.food.caloriesPer100g * (it.log.grams / 100.0)
                },
                items = groupItems.map {
                    LoggedMealItem(
                        foodId = it.log.foodId,
                        grams = it.log.grams,
                        mealType = it.log.mealType,
                    )
                },
            )
        }.sortedBy { it.timestamp }
    }
}
