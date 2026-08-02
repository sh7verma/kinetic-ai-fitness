package com.shverma.kinetic.ui.aichat

import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.local.entity.FoodLogEntity
import com.shverma.kinetic.data.local.entity.toUILog
import com.shverma.kinetic.data.model.ai.AILogResponse
import com.shverma.kinetic.data.network.ChatType
import com.shverma.kinetic.data.repository.DietAIRepository
import com.shverma.kinetic.data.repository.FoodResolver
import com.shverma.kinetic.utils.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AIChatController(
    private val chatRepository: DietAIRepository,
    private val foodResolver: FoodResolver,
    private val foodLogDao: FoodLogDao,
    private val foodDao: FoodDao,
    private val scope: CoroutineScope,
) : AIChatActions {
    private val _state = MutableStateFlow(AIChatState())
    override val state: StateFlow<AIChatState> = _state.asStateFlow()

    override fun onInputChange(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    override fun onChatTypeChange(type: ChatType) {
        _state.update { it.copy(chatType = type) }
    }

    override fun saveMeal(originalMeal: UIMeal, updatedMeal: UIMeal) {
        scope.launch {
            try {
                updatedMeal.items.forEach { item ->
                    val food = foodDao.findBestFood(item.name)
                    if (food != null) {
                        foodLogDao.insert(
                            FoodLogEntity(
                                foodId = food.foodId,
                                grams = item.grams,
                                timestamp = currentTimeMillis(),
                                mealType = updatedMeal.mealType,
                            ),
                        )
                    }
                }
                _state.update { currentState ->
                    val updatedMessages = currentState.messages.map { message ->
                        val aiLogs = message.aiLogs
                        if (aiLogs != null) {
                            val updatedMeals = aiLogs.meals.map { meal ->
                                if (meal == originalMeal) updatedMeal.copy(isSaved = true) else meal
                            }
                            val allSaved = updatedMeals.all { it.isSaved }
                            message.copy(aiLogs = aiLogs.copy(meals = updatedMeals, isSaved = allSaved))
                        } else {
                            message
                        }
                    }
                    currentState.copy(messages = updatedMessages)
                }
            } catch (_: Exception) {
                // The UI remains usable; the meal can be retried from its unsaved state.
            }
        }
    }

    override fun sendMessage() {
        val message = _state.value.inputText
        if (message.isBlank()) return

        _state.update {
            it.copy(
                messages = it.messages + ChatMessage(text = message, isUser = true),
                inputText = "",
                isTyping = true,
            )
        }

        scope.launch {
            try {
                val data = chatRepository.logFood(message)
                if (!validateFoodLog(data)) {
                    _state.update {
                        it.copy(
                            isTyping = false,
                            messages = it.messages + ChatMessage(
                                text = "Couldn't understand your meal. Try again.",
                                isUser = false,
                            ),
                        )
                    }
                    return@launch
                }

                val uiLog = runCatching {
                    data.toUILog(foodResolver = foodResolver)
                }.getOrElse {
                    _state.update {
                        it.copy(
                            isTyping = false,
                            messages = it.messages + ChatMessage(
                                text = "Failed to process meal data",
                                isUser = false,
                            ),
                        )
                    }
                    return@launch
                }

                _state.update {
                    it.copy(
                        isTyping = false,
                        messages = it.messages + ChatMessage(
                            text = null,
                            isUser = false,
                            aiLogs = uiLog,
                        ),
                    )
                }
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        isTyping = false,
                        messages = it.messages + ChatMessage(
                            text = "Something went wrong",
                            isUser = false,
                        ),
                    )
                }
            }
        }
    }
}

fun validateFoodLog(response: AILogResponse): Boolean {
    if (response.entries.isEmpty()) return false

    return response.entries.all { entry ->
        entry.items.isNotEmpty() && entry.items.all { item -> item.food.isNotBlank() }
    }
}
