package dev.jmoicano.multiplayer.core.network.datasource

import dev.jmoicano.multiplayer.core.network.model.SearchResponse

/** Contract for the remote data source used by search and track detail flows. */
interface RemoteDataSource {
    /** Searches tracks from the remote API using [term], [limit], and [offset]. */
    suspend fun searchTracks(
        term: String,
        limit: Int = 20,
        offset: Int = 0
    ): SearchResponse

    /** Fetches details for a specific track. */
    suspend fun getTrackDetails(trackId: Long): SearchResponse

    /** Fetches album content and its tracks from [collectionId]. */
    suspend fun getAlbumTracks(collectionId: Long): SearchResponse
}

