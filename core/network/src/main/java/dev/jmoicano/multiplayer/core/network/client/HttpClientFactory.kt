package dev.jmoicano.multiplayer.core.network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating and configuring HTTP client.
 *
 * Exposes a single shared [Json] configuration ([sharedJson]) used by both the
 * Ktor [ContentNegotiation] plugin and manual response parsing, ensuring consistent
 * serialization behaviour across the network layer.
 */
object HttpClientFactory {
    private const val ITUNES_API_BASE_URL = "https://itunes.apple.com"

    /**
     * Shared [Json] instance used for all serialization/deserialization in the network layer.
     * Centralised here to avoid duplicate instances with diverging settings.
     */
    val sharedJson = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /**
     * Creates and configures an HTTP client for the iTunes API.
     */
    fun createHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(sharedJson)
            }

            defaultRequest {
                url(ITUNES_API_BASE_URL)
            }
        }
    }
}

