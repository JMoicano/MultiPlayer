package dev.jmoicano.multiplayer.core.network.repository

import android.util.Log
import dev.jmoicano.multiplayer.core.network.datasource.RemoteDataSource
import dev.jmoicano.multiplayer.core.network.model.SearchResponse
import dev.jmoicano.multiplayer.core.network.model.Track
import javax.inject.Inject

/** Exposes track search and lookup operations above the remote data layer. */
interface SearchRepository {
    /** Searches tracks by term, with pagination support through [limit] and [offset]. */
    suspend fun searchTracks(
        query: String,
        limit: Int = 20,
        offset: Int = 0
    ): Result<SearchResponse>

    /** Returns album tracks for the given [collectionId]. */
    suspend fun getAlbumTracks(collectionId: Long): Result<List<Track>>

    /** Returns track details, when found. */
    suspend fun getTrackDetails(trackId: Long): Result<Track?>
}

/** Default [SearchRepository] implementation backed by [RemoteDataSource]. */
class DefaultSearchRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : SearchRepository {
    companion object {
        private const val TAG = "MPSearchRepo"
    }

    private fun logDebug(message: String) {
        runCatching { Log.d(TAG, message) }
    }

    private fun logWarn(message: String) {
        runCatching { Log.w(TAG, message) }
    }

    private fun logError(message: String, throwable: Throwable) {
        runCatching { Log.e(TAG, message, throwable) }
    }

    override suspend fun searchTracks(
        query: String,
        limit: Int,
        offset: Int
    ): Result<SearchResponse> = try {
        logDebug("searchTracks query='$query' limit=$limit offset=$offset")
        if (query.isBlank()) {
            logWarn("searchTracks aborted: blank query")
            Result.failure(IllegalArgumentException("Search query cannot be empty"))
        } else {
            val response = remoteDataSource.searchTracks(query, limit, offset)
            logDebug("searchTracks success resultCount=${response.resultCount}")
            Result.success(response)
        }
    } catch (e: Exception) {
        logError("searchTracks failed query='$query' offset=$offset: ${e.message}", e)
        Result.failure(e)
    }

    override suspend fun getTrackDetails(trackId: Long): Result<Track?> = try {
        val response = remoteDataSource.getTrackDetails(trackId)
        val track = response.results.firstOrNull()
        Result.success(track)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAlbumTracks(collectionId: Long): Result<List<Track>> = try {
        logDebug("getAlbumTracks collectionId=$collectionId")
        val response = remoteDataSource.getAlbumTracks(collectionId)
        val tracks = response.results.filter { it.wrapperType == "track" }
        logDebug("getAlbumTracks success trackCount=${tracks.size}")
        Result.success(tracks)
    } catch (e: Exception) {
        logError("getAlbumTracks failed collectionId=$collectionId: ${e.message}", e)
        Result.failure(e)
    }
}
