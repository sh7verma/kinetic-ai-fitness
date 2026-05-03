package com.shverma.kinetic.ui.aichat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.local.entity.MealEntity
import com.shverma.kinetic.data.model.AIResponse
import com.shverma.kinetic.data.model.LogEntry
import com.shverma.kinetic.data.model.MealItem
import com.shverma.kinetic.data.model.MealPlan
import com.shverma.kinetic.data.model.MultiLogResponse
import com.shverma.kinetic.data.model.SingleLogResponse
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.model.WorkoutPlan
import com.shverma.kinetic.data.model.toMealEntity
import com.shverma.kinetic.data.network.ChatType
import com.shverma.kinetic.data.preference.DataStoreHelper
import com.shverma.kinetic.data.repository.ChatRepository
import com.shverma.kinetic.data.repository.MealRepository
import com.shverma.kinetic.data.repository.UserProfileRepository
import com.shverma.kinetic.utils.toIsoDateString
import com.shverma.kinetic.utils.toTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val workoutPlan: WorkoutPlan? = null,
    val mealPlan: MealPlan? = null,
    val singleLog: SingleLogResponse? = null,
    val multiLog: MultiLogResponse? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AIChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isTyping: Boolean = false,
    val chatType: ChatType = ChatType.WORKOUT
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val mealRepository: MealRepository,
    private val userProfileRepository: UserProfileRepository,
    private val dataStoreHelper: DataStoreHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val initialChatTypeStr: String = savedStateHandle["chatType"] ?: "WORKOUT"
    private val initialChatType = try { ChatType.valueOf(initialChatTypeStr) } catch (e: Exception) { ChatType.WORKOUT }

    private val _state = MutableStateFlow(AIChatState(chatType = initialChatType))
    val state: StateFlow<AIChatState> = _state.asStateFlow()

    init {
        updateWelcomeMessage(initialChatType)
    }

    private fun updateWelcomeMessage(type: ChatType) {
        val welcomeMessage = when (type) {
            ChatType.WORKOUT, ChatType.LOG_WORKOUT -> "Hello! I'm your Kinetic Workout coach. How can I help you with your training today?"
            ChatType.MEALS, ChatType.LOG_MEAL -> "Hello! I'm your Kinetic Nutrition coach. How can I help you with your meals today?"
        }
        _state.update {
            it.copy(
                messages = it.messages + ChatMessage(welcomeMessage, false)
            )
        }
    }

    fun onChatTypeChange(type: ChatType) {
        if (_state.value.chatType == type) return
        _state.update { it.copy(chatType = type) }
    }


    fun onInputChange(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val currentText = _state.value.inputText
        if (currentText.isBlank()) return

        val userMessage = ChatMessage(currentText, true)
        _state.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isTyping = true
            )
        }

        viewModelScope.launch {
            val currentMealPlan = dataStoreHelper.mealPlan.firstOrNull()?.let {
                Json.encodeToString(it)
            } ?: ""
            val weeklyMeals = mealRepository.getMealsFromDate(Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000).toIsoDateString())
                .firstOrNull()?.let {
                    Json.encodeToString(it)
                } ?: ""
            val currentWorkoutPlan = dataStoreHelper.workoutPlan.firstOrNull()?.let {
                Json.encodeToString(it)
            } ?: ""
            
            chatRepository.chat(
                message = currentText,
                chatType = _state.value.chatType,
                weeklyWorkouts = "", // TODO: Get weekly workouts if needed
                currentMealPlan = currentMealPlan,
                weeklyMeals = weeklyMeals,
                currentWorkoutPlan = currentWorkoutPlan
            ).onSuccess { response ->
                val aiMessage = when (response) {
                    is AIResponse.TextAnswer -> ChatMessage(response.message, false)
                    is AIResponse.WorkoutPlan -> ChatMessage(
                        text = "I've generated a workout plan for you: ${response.data.workout.title}",
                        isUser = false,
                        workoutPlan = response.data.workout
                    )

                    is AIResponse.MealPlanResult -> ChatMessage(
                        text = "I've generated a meal plan for you!",
                        isUser = false,
                        mealPlan = response.data
                    )

                    is AIResponse.SingleLogResult -> ChatMessage(
                        text = "I've analyzed your food log.",
                        isUser = false,
                        singleLog = response.data
                    )

                    is AIResponse.MultiLogResult -> ChatMessage(
                        text = "I've analyzed your multiple food entries.",
                        isUser = false,
                        multiLog = response.data
                    )

                    is AIResponse.Error -> ChatMessage(response.message, false)
                }
                _state.update {
                    it.copy(
                        messages = it.messages + aiMessage,
                        isTyping = false
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        messages = it.messages + ChatMessage(e.message ?: "An unexpected error occurred. Please try again.", false),
                        isTyping = false
                    )
                }
            }
        }
    }

    fun saveMealPlan(mealPlan: MealPlan) {
        val date = Date().toIsoDateString()

        viewModelScope.launch {
            dataStoreHelper.saveMealPlan(mealPlan)
            mealPlan.meals.forEach { meal ->
                mealRepository.insertMeal(meal.toMealEntity(date))
            }
            
            userProfileRepository.getUserProfileData().firstOrNull()?.let { userProfile ->
                if (userProfile.uid.isNotBlank()) {
                    mealRepository.saveMealPlanToFirestore(userProfile.uid, mealPlan)
                }
            }
        }
    }

    fun saveWorkoutPlan(workoutPlan: WorkoutPlan) {
        viewModelScope.launch {
            dataStoreHelper.saveWorkoutPlan(workoutPlan)
        }
    }

    fun saveSingleLog(log: SingleLogResponse) {
        val now = Date()
        val date = now.toIsoDateString()

        viewModelScope.launch {
            val mealEntity = MealEntity(
                name = log.entry.mealTime,
                time = now.toTimeString(),
                date = date,
                items = listOf(
                    MealItem(
                        food = log.entry.food,
                        quantity = log.entry.quantity,
                        calories = log.entry.calories,
                        proteinG = log.entry.proteinG,
                        carbsG = log.entry.carbsG,
                        fatsG = log.entry.fatsG
                    )
                ),
                totalCalories = log.entry.calories,
                totalProtein = log.entry.proteinG,
                totalCarbs = log.entry.carbsG,
                totalFats = log.entry.fatsG
            )
            mealRepository.insertMeal(mealEntity)
        }
    }

    fun saveMultiLogEntry(entry: LogEntry) {
        val now = Date()
        val date = now.toIsoDateString()

        viewModelScope.launch {
            val mealEntity = MealEntity(
                name = entry.mealTime,
                time = now.toTimeString(),
                date = date,
                items = entry.items,
                totalCalories = entry.totalCalories,
                totalProtein = entry.totalProtein,
                totalCarbs = entry.totalCarbs,
                totalFats = entry.totalFats
            )
            mealRepository.insertMeal(mealEntity)
        }
    }

    fun saveAllMultiLog(multiLog: MultiLogResponse) {
        viewModelScope.launch {
            multiLog.entries.forEach { entry ->
                saveMultiLogEntry(entry)
            }
        }
    }

    fun discardMessage(message: ChatMessage) {
        _state.update {
            it.copy(messages = it.messages.filter { m -> m != message })
        }
    }
}
