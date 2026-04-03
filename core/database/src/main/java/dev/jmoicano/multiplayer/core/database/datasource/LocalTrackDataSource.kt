package dev.jmoicano.multiplayer.core.database.datasource

import dev.jmoicano.multiplayer.core.network.model.Track

/** Contract for reading and writing the local track cache. */
interface LocalTrackDataSource {
    /** Persists or updates the provided [tracks]. */
    suspend fun upsertTracks(tracks: List<Track>)

    /** Returns a track by id, when available in cache. */
    suspend fun getTrackById(trackId: Long): Track?

    /** Returns locally stored tracks for a collection/album. */
    suspend fun getTracksByCollection(collectionId: Long): List<Track>

    /** Searches cached tracks using the same pagination contract as the remote layer. */
    suspend fun searchTracks(query: String, limit: Int = 20, offset: Int = 0): List<Track>

    /** Clears the local cache entirely. */
    suspend fun clearAll()
}

