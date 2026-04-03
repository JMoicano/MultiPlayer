package dev.jmoicano.multiplayer.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for track details and related album track listing.
 *
 * Loads the main track first and, when possible, fetches the remaining tracks
 * from the corresponding album.
 */
@HiltViewModel
class TrackDetailsViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val _track = MutableStateFlow<Track?>(null)
    val track: StateFlow<Track?> = _track.asStateFlow()

    private val _albumTracks = MutableStateFlow<List<Track>>(emptyList())
    val albumTracks: StateFlow<List<Track>> = _albumTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Starts loading track details from [trackId]. */
    fun loadTrackDetails(trackId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = searchRepository.getTrackDetails(trackId)

            if (result.isSuccess) {
                val track = result.getOrNull()
                _track.value = track
                track?.collectionId?.let { loadAlbumTracks(it) }
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load track"
            }

            _isLoading.value = false
        }
    }

    /** Best-effort load of tracks from the related album. */
    private suspend fun loadAlbumTracks(collectionId: Long) {
        val result = searchRepository.getAlbumTracks(collectionId)
        if (result.isSuccess) {
            _albumTracks.value = result.getOrNull() ?: emptyList()
        }
    }

    /** Clears state exposed by the details screen. */
    fun clearTrack() {
        _track.value = null
        _albumTracks.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }
}

