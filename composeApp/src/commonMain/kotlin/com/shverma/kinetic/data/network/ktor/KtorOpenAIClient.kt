package com.shverma.kinetic.data.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.client.request.setBody

class KtorOpenAIClient internal constructor(
    private val httpClient: HttpClient,
) {
    suspend fun getChatCompletions(
        apiKey: String,
        request: KtorOpenAIRequest,
    ): KtorOpenAIResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
        header(HttpHeaders.Authorization, "Bearer $apiKey")
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()
}
