package com.shverma.kinetic.data.network.ktor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KtorOpenAIRequest(
    val model: String,
    val messages: List<KtorOpenAIMessage>,
    @SerialName("max_completion_tokens") val maxCompletionTokens: Int? = null,
)

@Serializable
data class KtorOpenAIMessage(
    val role: String,
    val content: String,
)

@Serializable
data class KtorOpenAIResponse(
    val id: String = "",
    val choices: List<KtorOpenAIChoice> = emptyList(),
    val usage: KtorOpenAIUsage? = null,
)

@Serializable
data class KtorOpenAIChoice(
    val message: KtorOpenAIMessage = KtorOpenAIMessage("assistant", ""),
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
data class KtorOpenAIUsage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0,
)
