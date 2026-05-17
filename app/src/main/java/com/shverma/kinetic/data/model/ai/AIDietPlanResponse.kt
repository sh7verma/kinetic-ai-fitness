package com.shverma.kinetic.data.model.ai

import kotlinx.serialization.Serializable

@Serializable
data class AIDietPlanResponse(
    val type: String = "diet_plan",
    val diet_plan: AIDietPlan,
    val confidence: Double? = null,
    val reasoning: String? = null
)

@Serializable
data class AIDietPlan(
    val date: String,
    val goal: String,
    val meals: List<AIMeal>
)

@Serializable
data class AIMeal(
    val name: String,
    val time: String,
    val items: List<AIFoodItem>
)

@Serializable
data class AIFoodItem(
    val food: String,
    val grams: String?=null
)

fun String.toGrams(): Double {
    return this
        .lowercase()
        .replace("grams", "")
        .replace("gram", "")
        .replace("gms", "")
        .replace("gm", "")
        .replace("g", "")
        .trim()
        .toDoubleOrNull() ?: 0.0
}

fun safeGrams(input: String): Double {
    val g = input.toGrams()
    return if (g in 10.0..1000.0) g else 100.0
}