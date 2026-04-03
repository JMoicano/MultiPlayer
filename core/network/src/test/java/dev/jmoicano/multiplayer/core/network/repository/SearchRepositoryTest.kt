package dev.jmoicano.multiplayer.core.network.repository

import dev.jmoicano.multiplayer.core.network.datasource.RemoteDataSource
import dev.jmoicano.multiplayer.core.network.datasource.NetworkException
import dev.jmoicano.multiplayer.core.network.model.SearchResponse
import dev.jmoicano.multiplayer.core.network.model.Track
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class SearchRepositoryTest {

    private lateinit var repository: SearchRepository
    private lateinit var mockDataSource: FakeRemoteDataSource

    @Before
    fun setUp() {
        mockDataSource = FakeRemoteDataSource()
        repository = DefaultSearchRepository(mockDataSource)
    }

    @Test
    fun searchTracks_withValidQuery_returnsSuccess() = runTest {
        // Arrange
        val query = "The Beatles"
        mockDataSource.setSearchResponse(
            SearchResponse(
                resultCount = 1,
                results = listOf(
                    Track(
                        trackId = 1,
                        artistName = "The Beatles",
                        trackName = "Let It Be"
                    )
                )
            )
        )

        // Act
        val result = repository.searchTracks(query)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.resultCount)
        assertEquals(1, result.getOrNull()?.results?.size)
    }

    @Test
    fun searchTracks_withEmptyQuery_returnsFailure() = runTest {
        // Act
        val result = repository.searchTracks("")

        // Assert
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun searchTracks_withBlankQuery_returnsFailure() = runTest {
        // Act
        val result = repository.searchTracks("   ")

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun searchTracks_withNetworkError_returnsFailure() = runTest {
        // Arrange
        mockDataSource.setNetworkError(true)

        // Act
        val result = repository.searchTracks("test")

        // Assert
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun searchTracks_withCustomLimit_passesLimitToDataSource() = runTest {
        // Arrange
        val customLimit = 50
        mockDataSource.setSearchResponse(SearchResponse())

        // Act
        repository.searchTracks("test", limit = customLimit)

        // Assert
        assertEquals(customLimit, mockDataSource.lastLimit)
    }

    @Test
    fun searchTracks_withOffset_passesOffsetToDataSource() = runTest {
        // Arrange
        val offset = 20
        mockDataSource.setSearchResponse(SearchResponse())

        // Act
        repository.searchTracks("test", offset = offset)

        // Assert
        assertEquals(offset, mockDataSource.lastOffset)
    }

    @Test
    fun getTrackDetails_withValidTrackId_returnsTrack() = runTest {
        // Arrange
        val expectedTrack = Track(
            trackId = 123,
            artistName = "Artist",
            trackName = "Song"
        )
        mockDataSource.setSearchResponse(
            SearchResponse(
                resultCount = 1,
                results = listOf(expectedTrack)
            )
        )

        // Act
        val result = repository.getTrackDetails(123)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedTrack, result.getOrNull())
    }

    @Test
    fun getTrackDetails_withNoResults_returnsNull() = runTest {
        // Arrange
        mockDataSource.setSearchResponse(SearchResponse())

        // Act
        val result = repository.getTrackDetails(999)

        // Assert
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun getTrackDetails_withNetworkError_returnsFailure() = runTest {
        // Arrange
        mockDataSource.setNetworkError(true)

        // Act
        val result = repository.getTrackDetails(123)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun getAlbumTracks_withValidCollectionId_returnsOnlyTrackEntries() = runTest {
        // Arrange – first result is the collection (album), rest are songs
        val collectionEntry = Track(
            wrapperType = "collection",
            trackId = 0,
            artistName = "Artist",
            trackName = "Album Title",
            collectionId = 1000,
        )
        val songEntry1 = Track(
            wrapperType = "track",
            trackId = 1,
            artistName = "Artist",
            trackName = "Song 1",
            collectionId = 1000,
        )
        val songEntry2 = Track(
            wrapperType = "track",
            trackId = 2,
            artistName = "Artist",
            trackName = "Song 2",
            collectionId = 1000,
        )
        mockDataSource.setAlbumResponse(
            SearchResponse(
                resultCount = 3,
                results = listOf(collectionEntry, songEntry1, songEntry2),
            )
        )

        // Act
        val result = repository.getAlbumTracks(1000)

        // Assert
        assertTrue(result.isSuccess)
        val tracks = result.getOrNull()!!
        assertEquals(2, tracks.size)
        assertTrue(tracks.all { it.wrapperType == "track" })
    }

    @Test
    fun getAlbumTracks_withNetworkError_returnsFailure() = runTest {
        // Arrange
        mockDataSource.setNetworkError(true)

        // Act
        val result = repository.getAlbumTracks(1000)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun getAlbumTracks_withEmptyResponse_returnsEmptyList() = runTest {
        // Arrange
        mockDataSource.setAlbumResponse(SearchResponse())

        // Act
        val result = repository.getAlbumTracks(1000)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
}

/**
 * Fake implementation of RemoteDataSource for testing
 */
class FakeRemoteDataSource : RemoteDataSource {
    private var searchResponse = SearchResponse()
    private var albumResponse = SearchResponse()
    private var shouldThrowError = false
    var lastLimit = 20
    var lastOffset = 0

    fun setSearchResponse(response: SearchResponse) {
        this.searchResponse = response
    }

    fun setAlbumResponse(response: SearchResponse) {
        this.albumResponse = response
    }

    fun setNetworkError(shouldError: Boolean) {
        this.shouldThrowError = shouldError
    }

    override suspend fun searchTracks(
        term: String,
        limit: Int,
        offset: Int
    ): SearchResponse {
        lastLimit = limit
        lastOffset = offset
        if (shouldThrowError) {
            throw NetworkException("Test network error")
        }
        return searchResponse
    }

    override suspend fun getTrackDetails(trackId: Long): SearchResponse {
        if (shouldThrowError) {
            throw NetworkException("Test network error")
        }
        return searchResponse
    }

    override suspend fun getAlbumTracks(collectionId: Long): SearchResponse {
        if (shouldThrowError) {
            throw NetworkException("Test network error")
        }
        return albumResponse
    }
}

