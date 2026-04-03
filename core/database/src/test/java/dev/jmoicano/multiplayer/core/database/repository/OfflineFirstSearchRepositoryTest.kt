package dev.jmoicano.multiplayer.core.database.repository

import dev.jmoicano.multiplayer.core.database.datasource.LocalTrackDataSource
import dev.jmoicano.multiplayer.core.network.model.SearchResponse
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OfflineFirstSearchRepositoryTest {

    private lateinit var remoteRepository: FakeRemoteSearchRepository
    private lateinit var localTrackDataSource: FakeLocalTrackDataSource
    private lateinit var repository: OfflineFirstSearchRepository

    @Before
    fun setUp() {
        remoteRepository = FakeRemoteSearchRepository()
        localTrackDataSource = FakeLocalTrackDataSource()
        repository = OfflineFirstSearchRepository(
            remoteRepository = remoteRepository,
            localTrackDataSource = localTrackDataSource,
        )
    }

    @Test
    fun `searchTracks caches remote result`() = runTest {
        val remoteTracks = listOf(
            Track(trackId = 1L, artistName = "Artist", trackName = "Song"),
        )
        remoteRepository.searchResult = Result.success(
            SearchResponse(resultCount = remoteTracks.size, results = remoteTracks),
        )

        val result = repository.searchTracks(query = "test", limit = 20, offset = 0)

        assertTrue(result.isSuccess)
        assertEquals(remoteTracks, result.getOrNull()?.results)
        assertEquals(remoteTracks, localTrackDataSource.storedTracks)
    }

    @Test
    fun `searchTracks returns cached results when remote fails`() = runTest {
        val cachedTracks = listOf(
            Track(trackId = 10L, artistName = "Cached Artist", trackName = "Cached Song"),
        )
        remoteRepository.searchResult = Result.failure(IllegalStateException("network"))
        localTrackDataSource.searchResult = cachedTracks

        val result = repository.searchTracks(query = "cached", limit = 20, offset = 0)

        assertTrue(result.isSuccess)
        assertEquals(cachedTracks, result.getOrNull()?.results)
        assertEquals(cachedTracks.size, result.getOrNull()?.resultCount)
    }

    @Test
    fun `getTrackDetails falls back to cache when remote fails`() = runTest {
        val cachedTrack = Track(trackId = 99L, artistName = "Local", trackName = "Offline")
        remoteRepository.detailsResult = Result.failure(IllegalStateException("network"))
        localTrackDataSource.trackById = cachedTrack

        val result = repository.getTrackDetails(99L)

        assertTrue(result.isSuccess)
        assertEquals(cachedTrack, result.getOrNull())
    }
}

private class FakeRemoteSearchRepository : SearchRepository {
    var searchResult: Result<SearchResponse> = Result.success(SearchResponse())
    var albumResult: Result<List<Track>> = Result.success(emptyList())
    var detailsResult: Result<Track?> = Result.success(null)

    override suspend fun searchTracks(query: String, limit: Int, offset: Int): Result<SearchResponse> {
        return searchResult
    }

    override suspend fun getAlbumTracks(collectionId: Long): Result<List<Track>> {
        return albumResult
    }

    override suspend fun getTrackDetails(trackId: Long): Result<Track?> {
        return detailsResult
    }
}

private class FakeLocalTrackDataSource : LocalTrackDataSource {
    var storedTracks: List<Track> = emptyList()
    var searchResult: List<Track> = emptyList()
    var trackById: Track? = null
    var tracksByCollection: List<Track> = emptyList()

    override suspend fun upsertTracks(tracks: List<Track>) {
        storedTracks = tracks
    }

    override suspend fun getTrackById(trackId: Long): Track? {
        return trackById
    }

    override suspend fun getTracksByCollection(collectionId: Long): List<Track> {
        return tracksByCollection
    }

    override suspend fun searchTracks(query: String, limit: Int, offset: Int): List<Track> {
        return searchResult
    }

    override suspend fun clearAll() {
        storedTracks = emptyList()
        searchResult = emptyList()
        trackById = null
        tracksByCollection = emptyList()
    }
}

