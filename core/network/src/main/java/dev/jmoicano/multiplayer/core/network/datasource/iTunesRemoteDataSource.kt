package dev.jmoicano.multiplayer.core.network.datasource

import android.util.Log
import dev.jmoicano.multiplayer.core.network.client.HttpClientFactory
import dev.jmoicano.multiplayer.core.network.model.SearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import javax.inject.Inject

/**
 * iTunes-specific implementation of [RemoteDataSource].
 *
 * Uses [HttpClientFactory.sharedJson] for response parsing, which is the same
 * [kotlinx.serialization.json.Json] instance registered in the Ktor content-negotiation
 * plugin, keeping serialisation settings consistent.
 */
class iTunesRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient,
) : RemoteDataSource {

    companion object {
        private const val TAG = "MPSearchRemote"
    }

    private fun logDebug(message: String) {
        runCatching { Log.d(TAG, message) }
    }

    private fun logError(message: String, throwable: Throwable) {
        runCatching { Log.e(TAG, message, throwable) }
    }

    override suspend fun searchTracks(
        term: String,
        limit: Int,
        offset: Int
    ): SearchResponse {
        return try {
            logDebug("searchTracks request term='$term' limit=$limit offset=$offset")
            val rawResponse = httpClient.get("/search") {
                parameter("term", term)
                parameter("entity", "song")
                parameter("media", "music")
                parameter("limit", limit)
                parameter("offset", offset)
            }.bodyAsText()
            val response = HttpClientFactory.sharedJson.decodeFromString<SearchResponse>(rawResponse)
            logDebug("searchTracks success resultCount=${response.resultCount}")
            response
        } catch (e: Exception) {
            logError("searchTracks error term='$term' offset=$offset: ${e.message}", e)
            throw NetworkException("Failed to search tracks: ${e.message}", e)
        }
    }

    override suspend fun getTrackDetails(trackId: Long): SearchResponse {
        return try {
            val rawResponse = httpClient.get("/lookup") {
                parameter("id", trackId)
                parameter("entity", "song")
            }.bodyAsText()
            HttpClientFactory.sharedJson.decodeFromString(rawResponse)
        } catch (e: Exception) {
            throw NetworkException("Failed to get track details: ${e.message}", e)
        }
    }

    override suspend fun getAlbumTracks(collectionId: Long): SearchResponse {
        return try {
            logDebug("getAlbumTracks collectionId=$collectionId")
            val rawResponse = httpClient.get("/lookup") {
                parameter("id", collectionId)
                parameter("entity", "song")
            }.bodyAsText()
            HttpClientFactory.sharedJson.decodeFromString(rawResponse)
        } catch (e: Exception) {
            logError("getAlbumTracks error collectionId=$collectionId: ${e.message}", e)
            throw NetworkException("Failed to get album tracks: ${e.message}", e)
        }
    }
}

/**
 * Custom exception for network errors.
 */
class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
