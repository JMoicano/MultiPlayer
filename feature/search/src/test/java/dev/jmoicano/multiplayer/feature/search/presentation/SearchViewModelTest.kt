package dev.jmoicano.multiplayer.feature.search.presentation

import dev.jmoicano.multiplayer.core.network.model.SearchResponse
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class SearchViewModelTest {

    @Test
    fun updateSearchQuery_setsQueryAndClearsError() = runTest {
        val viewModel = SearchViewModel(SearchViewModelFakeSearchRepository())

        viewModel.updateSearchQuery("The Beatles")

        assertEquals("The Beatles", viewModel.searchQuery.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun updateSearchQuery_withEmptyQuery_stillUpdates() = runTest {
        val viewModel = SearchViewModel(SearchViewModelFakeSearchRepository())

        viewModel.updateSearchQuery("")

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun clearSearch_resetsAllState() = runTest {
        val viewModel = SearchViewModel(SearchViewModelFakeSearchRepository())
        viewModel.updateSearchQuery("test")

        viewModel.clearSearch()

        assertEquals("", viewModel.searchQuery.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }
}

private class SearchViewModelFakeSearchRepository : SearchRepository {
    override suspend fun searchTracks(query: String, limit: Int, offset: Int): Result<SearchResponse> {
        return Result.success(SearchResponse())
    }

    override suspend fun getAlbumTracks(collectionId: Long): Result<List<Track>> = Result.success(emptyList())

    override suspend fun getTrackDetails(trackId: Long): Result<Track?> = Result.success(null)
}

