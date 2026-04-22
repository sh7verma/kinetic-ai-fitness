package com.shverma.kinetic.ui.plan
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(PlanState())
    val state: StateFlow<PlanState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: PlanEvents) {
        when (event) {
            is PlanEvents.LoadData -> loadData()
        }
    }

    private fun loadData() {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val todayIndex = when(currentDayOfWeek) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
        _state.value = _state.value.copy(
            streakDays = todayIndex,
            streakProgress = todayIndex,
            totalStreakDays = 7,
            dailyFocusTitle = "Neural Adaptation Phase",
            exercises = dummyExercises
        )
    }
}

sealed class PlanEvents {
    data object LoadData : PlanEvents()
}

data class PlanState(
    val streakDays: Int = 0,
    val streakProgress: Int = 0,
    val totalStreakDays: Int = 0,
    val dailyFocusTitle: String = "",
    val exercises: List<Exercise> = emptyList()
)

data class Exercise(
    val name: String,
    val tag: String,
    val sets: String,
    val reps: String,
    val weight: String,
    val isHighIntensity: Boolean = false
)

val dummyExercises = listOf(
    Exercise("Barbell Back Squat", "COMPOUND", "4", "8–10", "100 KG", true),
    Exercise("Leg Press", "STRENGTH", "3", "12", "180 KG"),
    Exercise("Leg Extensions", "ISOLATION", "3", "MAX", "60 KG")
)
