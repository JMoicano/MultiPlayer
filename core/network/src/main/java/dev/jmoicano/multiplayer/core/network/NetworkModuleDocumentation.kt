package dev.jmoicano.multiplayer.core.network

/**
 * Network Module Documentation
 *
 * This module provides all network-related functionality for the MultiPlayer app,
 * including the iTunes API integration for searching music.
 *
 * ## Architecture Overview
 *
 * The network module follows a layered, clean architecture:
 *
 * 1. **HTTP Client Layer**: [client.HttpClientFactory]
 *    - Configures Ktor HTTP client with JSON serialization
 *    - Sets up content negotiation for kotlinx.serialization
 *    - Manages OkHttp engine configuration
 *
 * 2. **Data Source Layer**: [datasource.RemoteDataSource] & [datasource.iTunesRemoteDataSource]
 *    - Abstract interface for network operations (dependency inversion)
 *    - iTunes-specific implementation using the iTunes Search API
 *    - Handles network exceptions and HTTP errors
 *
 * 3. **Repository Layer**: [repository.SearchRepository] & [repository.DefaultSearchRepository]
 *    - High-level API for search operations
 *    - Business logic and error handling
 *    - Input validation and Result<T> pattern
 *    - Separates concerns from UI layer
 *
 * 4. **Dependency Injection Layer**: [di.NetworkModule]
 *    - Exposes the default [io.ktor.client.HttpClient]
 *    - Binds [datasource.iTunesRemoteDataSource] to [datasource.RemoteDataSource]
 *    - Binds [repository.DefaultSearchRepository] as the remote [repository.SearchRepository]
 *    - Integrates with the app-wide Hilt graph used by the app variants
 *
 * ## Usage Examples
 *
 * ### Simple Search
 * ```kotlin
 * class SearchViewModel @Inject constructor(
 *     private val searchRepository: SearchRepository,
 * )
 *
 * val result = searchRepository.searchTracks("The Beatles")
 *
 * when {
 *     result.isSuccess -> {
 *         val tracks = result.getOrNull()?.results ?: emptyList()
 *         tracks.forEach { track ->
 *             println("${track.trackName} by ${track.artistName}")
 *         }
 *     }
 *     result.isFailure -> {
 *         val error = result.exceptionOrNull()
 *         println("Error: ${error?.message}")
 *     }
 * }
 * ```
 *
 * ### With Pagination
 * ```kotlin
 * val result = searchRepository.searchTracks(
 *     query = "The Beatles",
 *     limit = 20,
 *     offset = 0
 * )
 * ```
 *
 * ### Get Specific Track Details
 * ```kotlin
 * val trackResult = searchRepository.getTrackDetails(trackId = 123456)
 * val track = trackResult.getOrNull()
 * ```
 *
 * ## API Endpoints
 *
 * ### Search Tracks
 * - **Endpoint**: `/search`
 * - **Method**: GET
 * - **Parameters**:
 *   - `term`: Search query (artist name, song name, album, etc.)
 *   - `entity`: Set to "song" to search only music tracks
 *   - `media`: Set to "music" for music-only results
 *   - `limit`: Results per page (default: 20, max: 200)
 *   - `offset`: Pagination offset for skipping results
 *
 * ### Get Track Details (Lookup)
 * - **Endpoint**: `/lookup`
 * - **Method**: GET
 * - **Parameters**:
 *   - `id`: Track ID to lookup
 *   - `entity`: Set to "song" for song details
 *
 * ## Data Models
 *
 * - [dev.jmoicano.multiplayer.core.network.model.SearchResponse]: API response wrapper containing result count and track list
 * - [dev.jmoicano.multiplayer.core.network.model.Track]: Complete track data with 30+ fields including artwork, preview URL, duration, etc.
 *
 * ## Error Handling Strategy
 *
 * The module uses the Result<T> pattern for safe error handling:
 * - Network errors are wrapped in [dev.jmoicano.multiplayer.core.network.datasource.NetworkException]
 * - Repository methods return Result<T> for compose-safe error handling
 * - Input validation prevents empty queries
 * - HTTP errors are caught and transformed to domain exceptions
 *
 * ## Testing Approach
 *
 * - **HTTP Tests**: Use Ktor mock engine for deterministic HTTP testing
 * - **Repository Tests**: Use fake RemoteDataSource implementations
 * - **ViewModel Tests**: Leverage StateFlow collection for testing reactivity
 * - **Test Pattern**: All tests follow GIVEN-WHEN-THEN pattern
 * - **Coverage**: 16 unit tests with 100% pass rate
 *
 * ## Extensibility and DI
 *
 * To replace the iTunes implementation:
 *
 * 1. Create a new implementation of [datasource.RemoteDataSource]
 * 2. Bind it in your DI graph instead of [datasource.iTunesRemoteDataSource]
 * 3. Keep consumers depending on [repository.SearchRepository]
 *
 * **Current Hilt setup:**
 * ```kotlin
 * @Binds
 * @RemoteSearchRepository
 * abstract fun bindRemoteSearchRepository(
 *     repository: DefaultSearchRepository,
 * ): SearchRepository
 * ```
 *
 * This design ensures the network layer remains replaceable without affecting other modules.
 *
 * ## Integration with Paging 3
 *
 * For paginated UI integration, use [dev.jmoicano.multiplayer.feature.search.data.TrackSearchPagingSource]:
 * ```kotlin
 * val pager = Pager(
 *     config = PagingConfig(pageSize = 20),
 *     pagingSourceFactory = { TrackSearchPagingSource(repository, query) }
 * )
 * ```
 *
 * ## SOLID Principles Applied
 *
 * - **S**RP: Each class has a single responsibility
 * - **O**CP: Open for extension (new implementations), closed for modification
 * - **L**SP: Implementations are substitutable without breaking contracts
 * - **I**SP: Interfaces are focused and minimal
 * - **D**IP: Depends on abstractions, not concrete implementations
 */
object NetworkModuleDocumentation

