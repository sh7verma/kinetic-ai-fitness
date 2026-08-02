package com.shverma.kinetic.ui.aichat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.kinetic.data.local.dao.FoodDao
import com.shverma.kinetic.data.local.dao.FoodLogDao
import com.shverma.kinetic.data.repository.DietAIRepository
import com.shverma.kinetic.data.repository.FoodResolver
import kotlinx.coroutines.flow.StateFlow

/** Android lifecycle adapter for the shared AI-chat controller. */
class AIChatViewModel(
    chatRepository: DietAIRepository,
    foodResolver: FoodResolver,
    foodLogDao: FoodLogDao,
    foodDao: FoodDao,
) : ViewModel(), AIChatActions {
    private val controller = AIChatController(
        chatRepository = chatRepository,
        foodResolver = foodResolver,
        foodLogDao = foodLogDao,
        foodDao = foodDao,
        scope = viewModelScope,
    )

    override val state: StateFlow<AIChatState> = controller.state

    override fun onInputChange(text: String) = controller.onInputChange(text)

    override fun onChatTypeChange(type: com.shverma.kinetic.data.network.ChatType) =
        controller.onChatTypeChange(type)

    override fun saveMeal(originalMeal: UIMeal, updatedMeal: UIMeal) =
        controller.saveMeal(originalMeal, updatedMeal)

    override fun sendMessage() = controller.sendMessage()
}
