package com.shverma.kinetic.data.model.ai

import kotlinx.serialization.Serializable

@Serializable
data class AIFoodItem(
    val food: String,
    val grams: String? = null,
    val confidence: Double = 0.0,
    val assumed: String? = null
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