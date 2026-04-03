package dev.jmoicano.multiplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.jmoicano.multiplayer.core.database.model.TrackEntity

/** Room access object for tracks persisted in local cache. */
@Dao
interface TrackDao {
    /** Inserts or updates a list of track entities. */
    @Upsert
    suspend fun upsertTracks(tracks: List<TrackEntity>)

    /** Fetches a track by its unique iTunes id. */
    @Query("SELECT * FROM tracks WHERE trackId = :trackId LIMIT 1")
    suspend fun getTrackById(trackId: Long): TrackEntity?

    /** Returns tracks of a collection ordered by track number and name. */
    @Query(
        """
        SELECT * FROM tracks
        WHERE collectionId = :collectionId
        ORDER BY trackNumber ASC, trackName ASC
        """,
    )
    suspend fun getTracksByCollection(collectionId: Long): List<TrackEntity>

    /** Runs a paginated text search over cached tracks. */
    @Query(
        """
        SELECT * FROM tracks
        WHERE (
            :query = '' OR
            trackName LIKE '%' || :query || '%' COLLATE NOCASE OR
            artistName LIKE '%' || :query || '%' COLLATE NOCASE OR
            collectionName LIKE '%' || :query || '%' COLLATE NOCASE
        )
        ORDER BY trackName ASC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun searchTracks(query: String, limit: Int, offset: Int): List<TrackEntity>

    /** Deletes all data from the cache table. */
    @Query("DELETE FROM tracks")
    suspend fun clearAll()
}

