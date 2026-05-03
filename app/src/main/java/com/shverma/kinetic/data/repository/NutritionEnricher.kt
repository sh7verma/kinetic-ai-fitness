package com.shverma.kinetic.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.shverma.kinetic.BuildConfig
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.entity.FoodEntity
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
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionEnricher @Inject constructor(
    private val foodDao: FoodDao,
    private val firestore: FirebaseFirestore,
    private val foodResolver: FoodResolver,
    private val openAIService: OpenAIService
) {

    private val json = Json { ignoreUnknownKeys = true }

    // Metrics for monitoring
    private val apiCallCount = AtomicInteger(0)
    private val cacheHitCount = AtomicInteger(0)
    private val failureCount = AtomicInteger(0)

    companion object {
        private const val TAG = "NutritionEnricher"
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

    // 2. Check Room first (cached + USDA + AI results are all in Room)
        val neededFromApi = mutableListOf<MealItem>()
        val cachedResults = mutableMapOf<String, MealItem>()

        itemsToEnrich.forEach { item ->
            val resolvedName = foodResolver.resolve(item.food)
            val local = foodDao.searchFoods(resolvedName).firstOrNull()

            if (local != null) {
                cacheHitCount.incrementAndGet()
                val enriched = item.copy(
                    calories = local.calories,
                    proteinG = local.protein,
                    carbsG = local.carbs,
                    fatsG = local.fats
                )
                cachedResults[createCacheKey(item)] = enriched
            } else {
                neededFromApi.add(item)
            }
        }

        // 3. Fetch from API if needed
        if (neededFromApi.isNotEmpty()) {
            try {
                val resolvedResults = neededFromApi.map { item ->
                    async { resolveAndFetch(item) }
                }.awaitAll()

                resolvedResults.forEach { enriched ->
                    val key = createCacheKey(enriched)
                    cachedResults[key] = enriched
                }
            } catch (e: Exception) {
                Log.e(TAG, "Enrichment failed: ${e.message}")
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

    private suspend fun resolveAndFetch(item: MealItem): MealItem {
        val resolvedName = foodResolver.resolve(item.food)

        // 1. Local DB (Room) - Note: Already checked in enrichMealPlanWithNutrition,
        // but kept here for completeness if called directly.
        val local = foodDao.searchFoods(resolvedName).firstOrNull()
        if (local != null) {
            return item.copy(
                calories = local.calories,
                proteinG = local.protein,
                carbsG = local.carbs,
                fatsG = local.fats
            )
        }

        // 2. Firestore fallback
        val remote = fetchFromFirestore(resolvedName)
        if (remote != null) {
            return item.copy(
                calories = remote.calories,
                proteinG = remote.protein,
                carbsG = remote.carbs,
                fatsG = remote.fats
            )
        }

        // 3. AI fallback (LAST)
        return fetchSingleFromAI(item, resolvedName)
    }

    // 2. Try Firestore fallback
    private suspend fun fetchFromFirestore(name: String): FoodEntity? {
        return try {
            val doc = firestore.collection("foods")
                .document(name)
                .get()
                .await()

            if (doc.exists()) {
                val entity = FoodEntity(
                    name = doc.getString("name") ?: name,
                    calories = doc.getDouble("calories") ?: 0.0,
                    protein = doc.getDouble("protein") ?: 0.0,
                    carbs = doc.getDouble("carbs") ?: 0.0,
                    fats = doc.getDouble("fats") ?: 0.0,
                    source = "firestore"
                )
                foodDao.insert(entity)
                entity
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Firestore fetch failed for $name: ${e.message}")
            null
        }
    }

    private suspend fun fetchSingleFromAI(item: MealItem, resolvedName: String): MealItem {
        return try {
            val request = OpenAIRequest(
                model = "gpt-4o-mini",
                messages = listOf(
                    OpenAIMessage("system", "Return JSON ONLY."),
                    OpenAIMessage("user", AIPrompts.buildBatchPrompt(listOf(item)))
                ),
                maxCompletionTokens = 300
            )

            val response = openAIService.getChatCompletions(
                auth = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = request
            )

            val content = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("Empty AI response")

            val cleanedJson = content.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val list = json.decodeFromString<List<MealItem>>(cleanedJson)
            val aiResult = list.firstOrNull() ?: createFallbackItem(item)

            // Save AI result to Room
            foodDao.insert(
                FoodEntity(
                    name = resolvedName,
                    calories = aiResult.calories,
                    protein = aiResult.proteinG,
                    carbs = aiResult.carbsG,
                    fats = aiResult.fatsG,
                    source = "ai"
                )
            )

            aiResult
        } catch (e: Exception) {
            Log.e(TAG, "AI fallback failed for ${item.food}: ${e.message}")
            createFallbackItem(item)
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
