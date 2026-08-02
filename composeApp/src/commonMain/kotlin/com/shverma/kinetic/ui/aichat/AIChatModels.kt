package com.shverma.kinetic.ui.aichat

import com.shverma.kinetic.data.network.ChatType
import com.shverma.kinetic.utils.currentTimeMillis

data class UILog(
    val meals: List<UIMeal>,
    val isSaved: Boolean = false,
)

data class UIMeal(
    val mealType: String,
    val items: List<UIFoodItem>,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFats: Double,
    val isSaved: Boolean = false,
)

data class UIFoodItem(
    val name: String,
    val grams: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val confidence: Double = 0.0,
    val assumed: String? = null,
)

data class ChatMessage(
    val text: String? = null,
    val isUser: Boolean,
    val aiLogs: UILog? = null,
    val timestamp: Long = currentTimeMillis(),
)

data class AIChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isTyping: Boolean = false,
    val chatType: ChatType = ChatType.LOG_MEAL,
)
