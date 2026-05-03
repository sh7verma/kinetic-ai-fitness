package com.shverma.kinetic.data.repository

import android.util.Log
import com.shverma.kinetic.BuildConfig
import com.shverma.kinetic.data.model.Macros
import com.shverma.kinetic.data.model.MealItem
import com.shverma.kinetic.data.model.MealPlan
import com.shverma.kinetic.data.network.AIPrompts
import com.shverma.kinetic.data.network.OpenAIMessage
import com.shverma.kinetic.data.network.OpenAIRequest
import com.shverma.kinetic.data.network.OpenAIService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionEnricher @Inject constructor(
    private val openAIService: OpenAIService
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val semaphore = Semaphore(5) // Max 5 parallel calls

    // Thread-safe cache with TTL support
    private val nutritionCache = ConcurrentHashMap<String, CachedMealItem>()

    // Metrics for monitoring
    private val apiCallCount = AtomicInteger(0)
    private val cacheHitCount = AtomicInteger(0)
    private val failureCount = AtomicInteger(0)

    private data class CachedMealItem(
        val item: MealItem,
        val timestamp: Long = System.currentTimeMillis()
    )


    companion object {
        private const val TAG = "NutritionEnricher"
        private const val CACHE_TTL_MS = 10 * 60 * 1000L // 10 minutes
        private const val TIMEOUT_MS = 5000L
        private const val DEFAULT_CALORIES = 100.0
        private const val DEFAULT_PROTEIN = 5.0
        private const val DEFAULT_CARBS = 10.0
        private const val DEFAULT_FATS = 3.0
    }

    /**
     * Enriches a MealPlan with nutrition data using AI.
     */
    suspend fun enrichMealPlanWithNutrition(
        mealPlan: MealPlan
    ): MealPlan = coroutineScope {
        Log.d(TAG, "Starting MealPlan enrichment")

        // 1. Collect items to enrich (skip already enriched, deduplicate)
        val allItems = mealPlan.meals.flatMap { it.items }
        val itemsToEnrich = allItems
            .filter { it.calories <= 0 }
            .distinctBy { createCacheKey(it) }

        if (itemsToEnrich.isEmpty()) {
            return@coroutineScope mealPlan
        }

        // 2. Check cache first
        val neededFromApi = mutableListOf<MealItem>()
        val cachedResults = mutableMapOf<String, MealItem>()

        itemsToEnrich.forEach { item ->
            val key = createCacheKey(item)
            val cached = nutritionCache[key]
            if (cached != null && (System.currentTimeMillis() - cached.timestamp) < CACHE_TTL_MS) {
                cacheHitCount.incrementAndGet()
                cachedResults[key] = cached.item
            } else {
                neededFromApi.add(item)
            }
        }

        // 3. Fetch from API in batch if needed
        if (neededFromApi.isNotEmpty()) {
            try {
                val apiResults = fetchBatchNutrition(neededFromApi)
                apiResults.forEach { enriched ->
                    val key = createCacheKey(enriched)
                    nutritionCache[key] = CachedMealItem(enriched)
                    cachedResults[key] = enriched
                }
            } catch (e: Exception) {
                Log.e(TAG, "Batch enrichment failed: ${e.message}")
                failureCount.addAndGet(neededFromApi.size)
                neededFromApi.forEach { item ->
                    val key = createCacheKey(item)
                    val fallback = createFallbackItem(item)
                    cachedResults[key] = fallback
                }
            }
        }

        // 4. Map back to original structure and update totals
        val enrichedMeals = mealPlan.meals.map { meal ->
            val updatedItems = meal.items.map { item ->
                if (item.calories > 0) item
                else cachedResults[createCacheKey(item)] ?: item
            }
            meal.copy(items = updatedItems)
        }

        logMetrics()

        val totalCalories = enrichedMeals.sumOf { m -> m.items.sumOf { it.calories } }
        val totalProtein = enrichedMeals.sumOf { m -> m.items.sumOf { it.proteinG } }
        val totalCarbs = enrichedMeals.sumOf { m -> m.items.sumOf { it.carbsG } }
        val totalFats = enrichedMeals.sumOf { m -> m.items.sumOf { it.fatsG } }

        mealPlan.copy(
            meals = enrichedMeals,
            totalCalories = totalCalories,
            macros = Macros(
                proteinG = totalProtein,
                carbsG = totalCarbs,
                fatsG = totalFats
            )
        )
    }

    private suspend fun fetchBatchNutrition(
        items: List<MealItem>
    ): List<MealItem> {

        val request = OpenAIRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                OpenAIMessage("system", "Return JSON ONLY."),
                OpenAIMessage("user", AIPrompts.buildBatchPrompt(items))
            ),
            maxCompletionTokens = 1000 // Increased for batch
        )

        val response = openAIService.getChatCompletions(
            auth = "Bearer ${BuildConfig.OPENAI_API_KEY}",
            request = request
        )

        val content = response.choices.firstOrNull()?.message?.content
            ?: throw Exception("Empty batch response")

        return parseBatchNutritionJson(content, items)
    }

    private fun parseBatchNutritionJson(jsonStr: String, originalItems: List<MealItem>): List<MealItem> {
        return try {
            val cleanedJson = jsonStr.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val list = json.decodeFromString<List<MealItem>>(cleanedJson)
            
            // Map back to original items to preserve any other data if necessary
            // or just return the parsed list if it matches 1:1
            list
        } catch (e: Exception) {
            Log.e(TAG, "Batch parsing failed for $jsonStr", e)
            originalItems.map { createFallbackItem(it) }
        }
    }

    private fun createFallbackItem(item: MealItem) = item.copy(
        calories = DEFAULT_CALORIES,
        proteinG = DEFAULT_PROTEIN,
        carbsG = DEFAULT_CARBS,
        fatsG = DEFAULT_FATS
    )

    private fun createCacheKey(item: MealItem): String {
        return "${item.food.lowercase().trim()}_${item.quantity.lowercase().trim()}"
    }

    private fun logMetrics() {
        Log.d(TAG, "Metrics -> API Calls: ${apiCallCount.get()}, Cache Hits: ${cacheHitCount.get()}, Failures: ${failureCount.get()}")
    }
}
