package dev.jmoicano.multiplayer.core.database.repository

import dev.jmoicano.multiplayer.core.database.datasource.LocalTrackDataSource
import dev.jmoicano.multiplayer.core.network.di.RemoteSearchRepository
import dev.jmoicano.multiplayer.core.network.model.SearchResponse
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository
import javax.inject.Inject

/**
 * [SearchRepository] implementation that prioritizes remote responses but uses
 * local cache as fallback when network calls fail.
 */
class OfflineFirstSearchRepository @Inject constructor(
    @RemoteSearchRepository private val remoteRepository: SearchRepository,
    private val localTrackDataSource: LocalTrackDataSource,
) : SearchRepository {

    override suspend fun searchTracks(query: String, limit: Int, offset: Int): Result<SearchResponse> {
        val normalizedQuery = query.trim()
        val remoteResult = remoteRepository.searchTracks(
            query = normalizedQuery,
            limit = limit,
            offset = offset,
        )

        return remoteResult.fold(
            onSuccess = { response ->
                cacheTracks(response.results)
                Result.success(response)
            },
            onFailure = { remoteError ->
                runCatching {
                    localTrackDataSource.searchTracks(
                        query = normalizedQuery,
                        limit = limit,
                        offset = offset,
                    )
                }.fold(
                    onSuccess = { cachedTracks ->
                        if (cachedTracks.isNotEmpty()) {
                            Result.success(
                                SearchResponse(
                                    resultCount = cachedTracks.size,
                                    results = cachedTracks,
                                ),
                            )
                        } else {
                            Result.failure(remoteError)
                        }
                    },
                    onFailure = { localError ->
                        Result.failure(localError)
                    },
                )
            },
        )
    }

    override suspend fun getAlbumTracks(collectionId: Long): Result<List<Track>> {
        val remoteResult = remoteRepository.getAlbumTracks(collectionId)

        return remoteResult.fold(
            onSuccess = { tracks ->
                cacheTracks(tracks)
                Result.success(tracks)
            },
            onFailure = { remoteError ->
                runCatching {
                    localTrackDataSource.getTracksByCollection(collectionId)
                }.fold(
                    onSuccess = { cachedTracks ->
                        if (cachedTracks.isNotEmpty()) {
                            Result.success(cachedTracks)
                        } else {
                            Result.failure(remoteError)
                        }
                    },
                    onFailure = { localError ->
                        Result.failure(localError)
                    },
                )
            },
        )
    }

    override suspend fun getTrackDetails(trackId: Long): Result<Track?> {
        val remoteResult = remoteRepository.getTrackDetails(trackId)

        return remoteResult.fold(
            onSuccess = { track ->
                if (track != null) {
                    cacheTracks(listOf(track))
                }
                Result.success(track)
            },
            onFailure = { remoteError ->
                runCatching {
                    localTrackDataSource.getTrackById(trackId)
                }.fold(
                    onSuccess = { cachedTrack ->
                        if (cachedTrack != null) {
                            Result.success(cachedTrack)
                        } else {
                            Result.failure(remoteError)
                        }
                    },
                    onFailure = { localError ->
                        Result.failure(localError)
                    },
                )
            },
        )
    }

    private suspend fun cacheTracks(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        runCatching { localTrackDataSource.upsertTracks(tracks) }
    }
}

