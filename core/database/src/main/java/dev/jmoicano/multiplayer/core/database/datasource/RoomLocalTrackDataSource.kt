package dev.jmoicano.multiplayer.core.database.datasource

import dev.jmoicano.multiplayer.core.database.dao.TrackDao
import dev.jmoicano.multiplayer.core.database.mapper.toEntity
import dev.jmoicano.multiplayer.core.database.mapper.toTrack
import dev.jmoicano.multiplayer.core.network.model.Track
import javax.inject.Inject

/** [LocalTrackDataSource] implementation backed by [TrackDao]. */
class RoomLocalTrackDataSource @Inject constructor(
    private val trackDao: TrackDao,
) : LocalTrackDataSource {
    override suspend fun upsertTracks(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        trackDao.upsertTracks(tracks.map { it.toEntity() })
    }

    override suspend fun getTrackById(trackId: Long): Track? {
        return trackDao.getTrackById(trackId)?.toTrack()
    }

    override suspend fun getTracksByCollection(collectionId: Long): List<Track> {
        return trackDao.getTracksByCollection(collectionId).map { it.toTrack() }
    }

    override suspend fun searchTracks(query: String, limit: Int, offset: Int): List<Track> {
        return trackDao.searchTracks(query = query.trim(), limit = limit, offset = offset)
            .map { it.toTrack() }
    }

    override suspend fun clearAll() {
        trackDao.clearAll()
    }
}

