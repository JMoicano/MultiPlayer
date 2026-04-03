package dev.jmoicano.multiplayer.core.player.sync

import dev.jmoicano.multiplayer.core.network.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Keeps a process-wide snapshot of playlist + playback so phone UI and Auto service can sync.
 */
data class SharedPlaybackState(
    val playlist: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isLoopEnabled: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val query: String = "",
    val source: String = "unknown",
    val updatedAtMs: Long = 0L,
    val browseTracks: List<Track> = emptyList(),
    val browseQuery: String = "",
    val browseUpdatedAtMs: Long = 0L,
) {
    /** Currently selected track within [playlist], when available. */
    val currentTrack: Track?
        get() = playlist.getOrNull(currentIndex)
}

/** In-memory shared state used by UI, Auto service, and Wear integration. */
object SharedPlaybackStore {
    private val _state = MutableStateFlow(SharedPlaybackState())

    /** Read-only flow of the current shared state. */
    val state: StateFlow<SharedPlaybackState> = _state.asStateFlow()

    /** Publishes a new snapshot of the primary playback state. */
    fun publish(
        source: String,
        playlist: List<Track> = _state.value.playlist,
        currentIndex: Int = _state.value.currentIndex,
        isPlaying: Boolean = _state.value.isPlaying,
        isLoopEnabled: Boolean = _state.value.isLoopEnabled,
        positionMs: Long = _state.value.positionMs,
        durationMs: Long = _state.value.durationMs,
        query: String = _state.value.query,
    ) {
        _state.value = SharedPlaybackState(
            playlist = playlist,
            currentIndex = currentIndex.coerceIn(-1, playlist.lastIndex),
            isPlaying = isPlaying,
            isLoopEnabled = isLoopEnabled,
            positionMs = positionMs.coerceAtLeast(0L),
            durationMs = durationMs.coerceAtLeast(0L),
            query = query,
            source = source,
            updatedAtMs = System.currentTimeMillis(),
            browseTracks = _state.value.browseTracks,
            browseQuery = _state.value.browseQuery,
            browseUpdatedAtMs = _state.value.browseUpdatedAtMs,
        )
    }

    /** Publishes a snapshot of the list currently shown in the browse experience. */
    fun publishBrowseSnapshot(
        tracks: List<Track>,
        query: String,
    ) {
        val previous = _state.value
        _state.value = previous.copy(
            browseTracks = tracks,
            browseQuery = query,
            browseUpdatedAtMs = System.currentTimeMillis(),
        )
    }
}

