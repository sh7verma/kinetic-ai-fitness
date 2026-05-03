package com.shverma.kinetic.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutPlanResponse(
    val type: String = "workout",
    @SerialName("workout")
    val workout: WorkoutPlan
)

@Serializable
data class WorkoutPlan(
    val title: String,
    @SerialName("duration_mins")
    val durationMins: Int,
    val difficulty: String, // beginner|intermediate|advanced
    @SerialName("target_muscles")
    val targetMuscles: List<String>,
    val exercises: List<Exercise>
)

@Serializable
data class Exercise(
    val name: String,
    val sets: Int,
    val reps: String,
    @SerialName("rest_secs")
    val restSecs: Int,
    val equipment: String, // none|dumbbell|barbell|machine|cable
    val cue: String
)
