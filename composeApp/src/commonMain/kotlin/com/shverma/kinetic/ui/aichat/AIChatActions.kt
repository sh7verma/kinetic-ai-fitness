package com.shverma.kinetic.ui.aichat

import com.shverma.kinetic.data.network.ChatType
import kotlinx.coroutines.flow.StateFlow

interface AIChatActions {
    val state: StateFlow<AIChatState>

    fun onInputChange(text: String)
    fun onChatTypeChange(type: ChatType)
    fun saveMeal(originalMeal: UIMeal, updatedMeal: UIMeal)
    fun sendMessage()
}
