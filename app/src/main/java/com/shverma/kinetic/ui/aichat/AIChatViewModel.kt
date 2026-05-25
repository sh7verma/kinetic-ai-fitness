package com.shverma.kinetic.ui.aichat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.local.entity.FoodLogEntity
import com.shverma.kinetic.data.local.entity.toUILog
import com.shverma.kinetic.data.model.ai.AILogResponse
import com.shverma.kinetic.data.network.ChatType
import com.shverma.kinetic.data.repository.DietAIRepository
import com.shverma.kinetic.data.repository.FoodResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class UILog(
    val meals: List<UIMeal>,
    val isSaved: Boolean = false
)

data class UIMeal(
    val mealType: String,
    val items: List<UIFoodItem>,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFats: Double,
    val isSaved: Boolean = false
)

data class UIFoodItem(
    val name: String,
    val grams: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
)

data class ChatMessage(
    val text: String? = null,
    val isUser: Boolean,
    val aiLogs: UILog? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AIChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isTyping: Boolean = false,
    val chatType: ChatType = ChatType.LOG_MEAL
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val chatRepository: DietAIRepository,
    private val foodResolver: FoodResolver,
    private val foodLogDao: FoodLogDao,
    private val foodDao: FoodDao
) : ViewModel() {
    private val _state = MutableStateFlow(AIChatState())
    val state: StateFlow<AIChatState> = _state.asStateFlow()

    init {
        updateWelcomeMessage()
    }

    private fun updateWelcomeMessage() {
        val welcomeMessage =
            "Hello! I'm your Kinetic Nutrition coach. How can I help you with your meals today?"
        _state.update {
            it.copy(
                messages = it.messages + ChatMessage(welcomeMessage, false)
            )
        }
    }

    fun onInputChange(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun onChatTypeChange(type: ChatType) {
        _state.update { it.copy(chatType = type) }
    }

    fun saveMeal(originalMeal: UIMeal, updatedMeal: UIMeal) {
        viewModelScope.launch {
            try {
                updatedMeal.items.forEach { item ->
                    val food = foodDao.findBestFood(item.name)
                    if (food != null) {
                        foodLogDao.insert(
                            FoodLogEntity(
                                foodId = food.foodId,
                                grams = item.grams,
                                timestamp = System.currentTimeMillis(),
                                mealType = updatedMeal.mealType
                            )
                        )
                    }
                }
                // Update state to mark meal as saved and update its values
                _state.update { currentState ->
                    val updatedMessages = currentState.messages.map { message ->
                        if (message.aiLogs != null) {
                            val updatedMeals = message.aiLogs.meals.map { m ->
                                if (m == originalMeal) updatedMeal.copy(isSaved = true) else m
                            }
                            val allSaved = updatedMeals.all { it.isSaved }
                            message.copy(aiLogs = message.aiLogs.copy(meals = updatedMeals, isSaved = allSaved))
                        } else {
                            message
                        }
                    }
                    currentState.copy(messages = updatedMessages)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to save meal")
            }
        }
    }

    fun sendMessage() {
        val message = _state.value.inputText
        if (message.isBlank()) return

        Timber.d("sendMessage: User input = $message")

        _state.update {
            it.copy(
                messages = it.messages + ChatMessage(text = message, isUser = true),
                inputText = "",
                isTyping = true
            )
        }

        viewModelScope.launch {
            Timber.d("sendMessage: Calling chatRepository.logFood")

            _state.update { it.copy(isTyping = true) }
            try {
                val data = chatRepository.logFood(message)
                Timber.d("AI Response success, data = $data")

                // ✅ STEP 1: Validate AI response
                if (!validateFoodLog(data)) {
                    Timber.e("Invalid AI response")

                    _state.update {
                        it.copy(
                            isTyping = false,
                            messages = it.messages + ChatMessage(
                                text = "Couldn't understand your meal. Try again.",
                                isUser = false
                            )
                        )
                    }
                    return@launch
                }

                // ✅ STEP 2: Safe parsing to UI model
                val uiLog = runCatching {
                    data.toUILog(foodResolver = foodResolver)
                }.getOrElse { e ->
                    Timber.e(e, "UILog parsing failed")

                    _state.update {
                        it.copy(
                            isTyping = false,
                            messages = it.messages + ChatMessage(
                                text = "Failed to process meal data",
                                isUser = false
                            )
                        )
                    }
                    return@launch
                }

                Timber.d("Parsed UILog = $uiLog")

                val aiMessage = ChatMessage(
                    text = null,
                    isUser = false,
                    aiLogs = uiLog
                )

                _state.update {
                    it.copy(
                        isTyping = false,
                        messages = it.messages + aiMessage
                    )
                }

            } catch (e: Exception) {
                Timber.e(e, "AI call failed")
                _state.update {
                    it.copy(
                        isTyping = false,
                        messages = it.messages + ChatMessage(
                            text = "Something went wrong",
                            isUser = false
                        )
                    )
                }
            }
        }
    }
}

fun validateFoodLog(response: AILogResponse): Boolean {
    if (response.entries.isEmpty()) return false

    return response.entries.all { entry ->
        entry.items.isNotEmpty() &&
                entry.items.all { item ->
                    item.food.isNotBlank()
                }
    }
}
