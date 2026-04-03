package dev.jmoicano.multiplayer.core.network.datasource

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*

class iTunesRemoteDataSourceTest {

    @Test
    fun searchTracks_withValidResponse_returnsParsedData() = runTest {
        // Arrange
        val mockResponse = """
            {
                "resultCount": 1,
                "results": [
                    {
                        "trackId": 123,
                        "artistName": "Test Artist",
                        "trackName": "Test Track"
                    }
                ]
            }
        """.trimIndent()

        val httpClient = createMockHttpClient(mockResponse)
        val dataSource = iTunesRemoteDataSource(httpClient)

        // Act
        val result = dataSource.searchTracks("test")

        // Assert
        assertEquals(1, result.resultCount)
        assertEquals(1, result.results.size)
        assertEquals(123, result.results[0].trackId)
        assertEquals("Test Artist", result.results[0].artistName)
        assertEquals("Test Track", result.results[0].trackName)
    }

    @Test
    fun searchTracks_withEmptyResults_returnsEmptyList() = runTest {
        // Arrange
        val mockResponse = """
            {
                "resultCount": 0,
                "results": []
            }
        """.trimIndent()

        val httpClient = createMockHttpClient(mockResponse)
        val dataSource = iTunesRemoteDataSource(httpClient)

        // Act
        val result = dataSource.searchTracks("nonexistent")

        // Assert
        assertEquals(0, result.resultCount)
        assertTrue(result.results.isEmpty())
    }

    @Test
    fun searchTracks_withTextJavascriptContentType_parsesSuccessfully() = runTest {
        // Arrange
        val mockResponse = """
            {
                "resultCount": 1,
                "results": [
                    {
                        "trackId": 999,
                        "artistName": "The Beatles",
                        "trackName": "Hey Jude"
                    }
                ]
            }
        """.trimIndent()

        val httpClient = createMockHttpClient(
            responseBody = mockResponse,
            contentType = "text/javascript; charset=utf-8",
        )
        val dataSource = iTunesRemoteDataSource(httpClient)

        // Act
        val result = dataSource.searchTracks("Beatles")

        // Assert
        assertEquals(1, result.resultCount)
        assertEquals("Hey Jude", result.results.first().trackName)
    }

    @Test
    fun searchTracks_withHttpError_throwsNetworkException() = runTest {
        // Arrange
        val httpClient = createMockHttpClientWithError()
        val dataSource = iTunesRemoteDataSource(httpClient)

        // Act & Assert
        try {
            dataSource.searchTracks("test")
            fail("Should have thrown NetworkException")
        } catch (e: NetworkException) {
            assertNotNull(e)
        }
    }

    @Test
    fun getTrackDetails_withValidTrackId_returnsTrackDetails() = runTest {
        // Arrange
        val mockResponse = """
            {
                "resultCount": 1,
                "results": [
                    {
                        "trackId": 456,
                        "artistName": "Artist",
                        "trackName": "Song",
                        "artworkUrl100": "https://example.com/image.jpg"
                    }
                ]
            }
        """.trimIndent()

        val httpClient = createMockHttpClient(mockResponse)
        val dataSource = iTunesRemoteDataSource(httpClient)

        // Act
        val result = dataSource.getTrackDetails(456)

        // Assert
        assertEquals(1, result.resultCount)
        assertEquals(456, result.results[0].trackId)
        assertEquals("Artist", result.results[0].artistName)
    }

    @Test
    fun getAlbumTracks_withCollectionEntryMissingTrackFields_parsesSuccessfully() = runTest {
        // Arrange
        val mockResponse = """
            {
                "resultCount": 2,
                "results": [
                    {
                        "wrapperType": "collection",
                        "collectionId": 1441133100,
                        "collectionName": "Greatest Hits",
                        "artistName": "The Artist"
                    },
                    {
                        "wrapperType": "track",
                        "trackId": 123456,
                        "trackName": "Hit Song",
                        "artistName": "The Artist",
                        "collectionId": 1441133100
                    }
                ]
            }
        """.trimIndent()

        val httpClient = createMockHttpClient(mockResponse)
        val dataSource = iTunesRemoteDataSource(httpClient)

        // Act
        val result = dataSource.getAlbumTracks(1441133100)

        // Assert
        assertEquals(2, result.resultCount)
        assertEquals(2, result.results.size)
        assertEquals("collection", result.results[0].wrapperType)
        assertEquals(-1L, result.results[0].trackId)
        assertEquals("", result.results[0].trackName)
        assertEquals("track", result.results[1].wrapperType)
        assertEquals(123456, result.results[1].trackId)
        assertEquals("Hit Song", result.results[1].trackName)
    }

    private fun createMockHttpClient(
        responseBody: String,
        contentType: String = ContentType.Application.Json.toString(),
    ): HttpClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, contentType)
            )
        }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = false
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }

            defaultRequest {
                url("https://itunes.apple.com")
            }
        }
    }

    private fun createMockHttpClientWithError(): HttpClient {
        val mockEngine = MockEngine { _ ->
            throw Exception("Network error")
        }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            defaultRequest {
                url("https://itunes.apple.com")
            }
        }
    }
}
