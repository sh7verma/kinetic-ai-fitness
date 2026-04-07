package com.shverma.kinetic.ui.logexercise

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LogExerciseViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(LogExerciseState())
    val state: StateFlow<LogExerciseState> = _state.asStateFlow()

    fun onEvent(event: LogExerciseEvent) {
        when (event) {
            is LogExerciseEvent.IncrementSets -> {
                _state.value = _state.value.copy(sets = _state.value.sets + 1)
            }
            is LogExerciseEvent.DecrementSets -> {
                if (_state.value.sets > 1) {
                    _state.value = _state.value.copy(sets = _state.value.sets - 1)
                }
            }
            is LogExerciseEvent.IncrementReps -> {
                _state.value = _state.value.copy(reps = _state.value.reps + 1)
            }
            is LogExerciseEvent.DecrementReps -> {
                if (_state.value.reps > 0) {
                    _state.value = _state.value.copy(reps = _state.value.reps - 1)
                }
            }
            is LogExerciseEvent.IncrementWeight -> {
                _state.value = _state.value.copy(weight = _state.value.weight + 2.5f)
            }
            is LogExerciseEvent.DecrementWeight -> {
                if (_state.value.weight >= 2.5f) {
                    _state.value = _state.value.copy(weight = _state.value.weight - 2.5f)
                }
            }
            is LogExerciseEvent.LogSet -> {
                // Handle logging set logic if needed
            }
        }
    }
}

data class LogExerciseState(
    val exerciseName: String = "EXPLOSIVE BARBELL SQUAT",
    val exerciseTag: String = "COMPOUND",
    val sets: Int = 1,
    val reps: Int = 8,
    val weight: Float = 100f,
    val lastPerformance: String = "LAST TIME: 4 SETS | 8 REPS | 100 KG",
    val totalSets: Int = 4,
    val targetReps: Int = 8,
    val targetWeight: Float = 100f
)

sealed class LogExerciseEvent {
    object IncrementSets : LogExerciseEvent()
    object DecrementSets : LogExerciseEvent()
    object IncrementReps : LogExerciseEvent()
    object DecrementReps : LogExerciseEvent()
    object IncrementWeight : LogExerciseEvent()
    object DecrementWeight : LogExerciseEvent()
    object LogSet : LogExerciseEvent()
}
