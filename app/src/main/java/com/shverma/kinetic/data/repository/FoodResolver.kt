package com.shverma.kinetic.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodResolver @Inject constructor() {

    private val stopWords = setOf(
        "grilled", "fried", "boiled", "roasted", "baked",
        "fresh", "raw", "with", "and", "in", "on",
        "pieces", "piece", "slice", "slices", "cup",
        "small", "medium", "large"
    )

    private val foodMap = mapOf(
        // Chicken
        "chicken breast" to "chicken",
        "chicken curry" to "chicken",
        "grilled chicken" to "chicken",

        // Eggs
        "eggs" to "egg",
        "egg white" to "egg",
        "boiled egg" to "egg",

        // Rice
        "brown rice" to "rice",
        "white rice" to "rice",
        "basmati rice" to "rice",

        // Oats
        "oatmeal" to "oats",

        // Dairy
        "yoghurt" to "yogurt",
        "curd" to "yogurt",

        // Indian basics (extend later)
        "paneer curry" to "paneer",
        "dal" to "lentils",
        "chapati" to "roti"
    )

    /**
     * Main function to resolve food name
     */
    fun resolve(input: String): String {
        val normalized = normalize(input)
        val cleaned = removeStopWords(normalized)

        // 1. Direct mapping
        foodMap[cleaned]?.let { return it }

        // 2. Try partial mapping
        foodMap.entries.firstOrNull { cleaned.contains(it.key) }?.let {
            return it.value
        }

        // 3. Fallback to best keyword
        return extractBestKeyword(cleaned)
    }

    /**
     * Normalize string
     */
    private fun normalize(input: String): String {
        return input
            .lowercase()
            .replace(Regex("[^a-z ]"), "")
            .trim()
    }

    /**
     * Remove noise words
     */
    private fun removeStopWords(text: String): String {
        return text.split(" ")
            .filter { it.isNotBlank() && it !in stopWords }
            .joinToString(" ")
    }

    /**
     * Pick most meaningful word
     */
    private fun extractBestKeyword(text: String): String {
        val words = text.split(" ")

        return words
            .filter { it.length > 3 }
            .maxByOrNull { it.length }
            ?: text
    }
}