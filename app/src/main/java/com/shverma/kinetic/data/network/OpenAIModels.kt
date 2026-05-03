package com.shverma.kinetic.data.network

import com.google.gson.annotations.SerializedName

data class OpenAIRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<OpenAIMessage>,
    @SerializedName("max_completion_tokens") val maxCompletionTokens: Int? = null,
)

data class OpenAIMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class OpenAIResponse(
    @SerializedName("id") val id: String,
    @SerializedName("choices") val choices: List<OpenAIChoice>,
    @SerializedName("usage") val usage: OpenAIUsage
)

data class OpenAIChoice(
    @SerializedName("message") val message: OpenAIMessage,
    @SerializedName("finish_reason") val finishReason: String
)

data class OpenAIUsage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)
