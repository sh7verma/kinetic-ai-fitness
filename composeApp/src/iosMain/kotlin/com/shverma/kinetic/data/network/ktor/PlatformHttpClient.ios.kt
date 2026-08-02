package com.shverma.kinetic.data.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createPlatformKtorOpenAIClient(): KtorOpenAIClient =
    KtorOpenAIClient(createConfiguredHttpClient(Darwin.create()))
