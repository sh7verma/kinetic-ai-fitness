package com.shverma.kinetic.data.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createPlatformKtorOpenAIClient(): KtorOpenAIClient =
    KtorOpenAIClient(createConfiguredHttpClient(OkHttp.create()))
