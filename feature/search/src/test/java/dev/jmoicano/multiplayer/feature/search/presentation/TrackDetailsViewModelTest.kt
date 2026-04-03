package dev.jmoicano.multiplayer.feature.search.presentation

import dev.jmoicano.multiplayer.core.network.datasource.NetworkException
import dev.jmoicano.multiplayer.core.network.model.SearchResponse
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TrackDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: TrackDetailsViewModel
    private lateinit var fakeRepository: FakeSearchRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSearchRepository()
        viewModel = TrackDetailsViewModel(searchRepository = fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadTrackDetails_withValidTrackId_setsTrack() = runTest {
        val expectedTrack = Track(
            trackId = 123L,
            artistName = "Artist",
            trackName = "Song",
        )
        fakeRepository.setTrackToReturn(expectedTrack)

        viewModel.loadTrackDetails(123L)
        advanceUntilIdle()

        assertEquals(expectedTrack, viewModel.track.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun loadTrackDetails_onNetworkError_setsErrorMessage() = runTest {
        fakeRepository.setShouldThrowError(true)

        viewModel.loadTrackDetails(999L)
        advanceUntilIdle()

        assertNull(viewModel.track.value)
        assertNotNull(viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun clearTrack_resetsState() = runTest {
        viewModel.clearTrack()

        assertNull(viewModel.track.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }
}

/**
 * Fake implementation of [SearchRepository] for unit-testing the presentation layer.
 */
class FakeSearchRepository : SearchRepository {
    private var trackToReturn: Track? = null
    private var albumTracksToReturn: List<Track> = emptyList()
    private var shouldThrowError = false

    fun setTrackToReturn(track: Track?) { this.trackToReturn = track }
    fun setAlbumTracksToReturn(tracks: List<Track>) { this.albumTracksToReturn = tracks }
    fun setShouldThrowError(shouldThrow: Boolean) { this.shouldThrowError = shouldThrow }

    override suspend fun searchTracks(
        query: String,
        limit: Int,
        offset: Int,
    ): Result<SearchResponse> = if (shouldThrowError) {
        Result.failure(NetworkException("Test error"))
    } else {
        Result.success(SearchResponse())
    }

    override suspend fun getTrackDetails(trackId: Long): Result<Track?> =
        if (shouldThrowError) {
            Result.failure(NetworkException("Test error"))
        } else {
            Result.success(trackToReturn)
        }

    override suspend fun getAlbumTracks(collectionId: Long): Result<List<Track>> =
        if (shouldThrowError) {
            Result.failure(NetworkException("Test error"))
        } else {
            Result.success(albumTracksToReturn)
        }
}
