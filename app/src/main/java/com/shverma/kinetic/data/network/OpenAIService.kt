package com.shverma.kinetic.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletions(
        @Header("Authorization") auth: String,
        @Body request: OpenAIRequest
    ): OpenAIResponse
}
