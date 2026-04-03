package dev.jmoicano.multiplayer.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository
import dev.jmoicano.multiplayer.core.player.sync.SharedPlaybackStore
import dev.jmoicano.multiplayer.feature.search.data.TrackSearchPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * ViewModel for the search and track browsing screen.
 *
 * Produces a paginated [tracks] flow derived from [searchQuery], with debounce to
 * avoid excessive API calls while typing.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val tracks: Flow<PagingData<Track>> = searchQuery
        .debounce(300)
        .map { it.trim() }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                _isLoading.value = false
                _error.value = null
                flowOf(PagingData.empty())
            } else {
                _isLoading.value = true
                _error.value = null
                Pager(
                    config = PagingConfig(
                        pageSize = 20,
                        enablePlaceholders = false,
                    ),
                    pagingSourceFactory = {
                        TrackSearchPagingSource(searchRepository, query)
                    },
                ).flow
            }
        }
        .cachedIn(viewModelScope)

    /** Updates the active query and clears any residual error state. */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _error.value = null
    }

    /** Clears the current search and resets the shared browse snapshot. */
    fun clearSearch() {
        _searchQuery.value = ""
        _error.value = null
        _isLoading.value = false
        SharedPlaybackStore.publishBrowseSnapshot(
            tracks = emptyList(),
            query = "",
        )
    }

    /** Publishes a reduced snapshot of the currently visible list to shared state. */
    fun syncBrowseSnapshot(tracks: List<Track>) {
        SharedPlaybackStore.publishBrowseSnapshot(
            tracks = tracks.take(50),
            query = _searchQuery.value.trim(),
        )
    }
}

