package dev.jmoicano.multiplayer.feature.player.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository
import dev.jmoicano.multiplayer.core.player.playback.ExoPlayerFactory
import dev.jmoicano.multiplayer.core.player.sync.SharedPlaybackState
import dev.jmoicano.multiplayer.core.player.sync.SharedPlaybackStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val albumTitle: String = "Album",
    val currentTrack: Track? = null,
    val playlist: List<Track> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isLoopEnabled: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val searchRepository: SearchRepository,
    private val exoPlayerFactory: ExoPlayerFactory,
) : AndroidViewModel(application) {

    companion object {
        private const val SYNC_SOURCE = "phone_player_ui"
        private const val EXTERNAL_SERVICE_SOURCE = "auto_phone_service"
    }

    private val player = exoPlayerFactory.create(application.applicationContext)

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null
    private var loadedTrackId: Long? = null
    private var isApplyingSharedState: Boolean = false
    private var playbackOwnerSource: String = SYNC_SOURCE

    init {
        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                    if (!isApplyingSharedState) publishSharedState()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    val duration = if (player.duration > 0) player.duration else 0L
                    _uiState.value = _uiState.value.copy(durationMs = duration)
                    if (!isApplyingSharedState) publishSharedState()
                }
            },
        )
        observeSharedState()
        startProgressUpdates()
    }

    fun load(trackId: Long) {
        val sharedState = SharedPlaybackStore.state.value
        if (playbackOwnerSource == EXTERNAL_SERVICE_SOURCE && sharedState.currentTrack?.trackId == trackId) {
            applyExternalSharedState(sharedState)
            return
        }

        playbackOwnerSource = SYNC_SOURCE

        val currentState = _uiState.value
        val existingIndex = currentState.playlist.indexOfFirst { it.trackId == trackId }
        if (existingIndex >= 0 && currentState.playlist.isNotEmpty()) {
            val shouldResumePlaying = player.isPlaying || player.playWhenReady
            if (player.mediaItemCount > 0 && existingIndex in 0 until player.mediaItemCount) {
                if (player.currentMediaItemIndex != existingIndex) {
                    player.seekTo(existingIndex, 0L)
                }
                player.playWhenReady = shouldResumePlaying
            }

            loadedTrackId = trackId
            _uiState.value = currentState.copy(
                isLoading = false,
                error = null,
                albumTitle = currentState.currentTrack?.collectionName ?: currentState.albumTitle,
                currentIndex = existingIndex,
                currentTrack = currentState.playlist[existingIndex],
                isPlaying = player.isPlaying,
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = if (player.duration > 0) player.duration else currentState.durationMs,
            )
            publishSharedState()
            return
        }

        if (loadedTrackId == trackId && currentState.playlist.isNotEmpty() && currentState.currentTrack != null) {
            _uiState.value = currentState.copy(
                isLoading = false,
                error = null,
                isPlaying = player.isPlaying,
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = if (player.duration > 0) player.duration else currentState.durationMs,
            )
            publishSharedState()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val trackResult = searchRepository.getTrackDetails(trackId)
            val selectedTrack = trackResult.getOrNull()
            if (selectedTrack == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = trackResult.exceptionOrNull()?.message ?: "Failed to load track",
                )
                return@launch
            }

            val playlist = selectedTrack.collectionId?.let { collectionId ->
                searchRepository.getAlbumTracks(collectionId).getOrNull().orEmpty()
            }.orEmpty()

            val safePlaylist = if (playlist.isEmpty()) listOf(selectedTrack) else playlist
            val startIndex = safePlaylist.indexOfFirst { it.trackId == selectedTrack.trackId }.coerceAtLeast(0)

            player.stop()
            player.clearMediaItems()
            safePlaylist.forEach { track ->
                track.previewUrl?.let { url ->
                    player.addMediaItem(MediaItem.fromUri(url))
                }
            }

            if (player.mediaItemCount > 0) {
                val safeStartIndex = startIndex.coerceAtMost(player.mediaItemCount - 1)
                player.seekTo(safeStartIndex, 0L)
                player.prepare()
                player.playWhenReady = true
            }

            loadedTrackId = trackId

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                albumTitle = selectedTrack.collectionName ?: "Album",
                currentTrack = safePlaylist.getOrNull(startIndex) ?: selectedTrack,
                playlist = safePlaylist,
                currentIndex = startIndex,
                isPlaying = player.isPlaying,
                durationMs = if (player.duration > 0) player.duration else 0L,
                positionMs = 0L,
            )
            publishSharedState()
        }
    }

    fun togglePlayPause() {
        if (playbackOwnerSource == EXTERNAL_SERVICE_SOURCE) return
        if (player.isPlaying) player.pause() else player.play()
        publishSharedState()
    }

    fun playNext() {
        if (playbackOwnerSource == EXTERNAL_SERVICE_SOURCE) return
        val state = _uiState.value
        if (state.playlist.isEmpty()) return
        val nextIndex = (state.currentIndex + 1) % state.playlist.size
        seekToTrack(nextIndex)
    }

    fun playPrevious() {
        if (playbackOwnerSource == EXTERNAL_SERVICE_SOURCE) return
        val state = _uiState.value
        if (state.playlist.isEmpty()) return
        val previousIndex = if (state.currentIndex == 0) state.playlist.lastIndex else state.currentIndex - 1
        seekToTrack(previousIndex)
    }

    fun playTrackAt(index: Int) {
        if (playbackOwnerSource == EXTERNAL_SERVICE_SOURCE) return
        val state = _uiState.value
        if (state.playlist.isEmpty() || index !in state.playlist.indices) return
        seekToTrack(index)
    }

    fun toggleLoop() {
        if (playbackOwnerSource == EXTERNAL_SERVICE_SOURCE) return
        val enabled = !_uiState.value.isLoopEnabled
        player.repeatMode = if (enabled) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        _uiState.value = _uiState.value.copy(isLoopEnabled = enabled)
        publishSharedState()
    }

    fun seekToProgress(progress: Float) {
        if (playbackOwnerSource == EXTERNAL_SERVICE_SOURCE) return
        val duration = _uiState.value.durationMs
        if (duration <= 0L) return
        player.seekTo((duration * progress.coerceIn(0f, 1f)).toLong())
        publishSharedState()
    }

    private fun seekToTrack(index: Int) {
        if (player.mediaItemCount > 0 && index in 0 until player.mediaItemCount) {
            player.seekTo(index, 0L)
            player.playWhenReady = true
        }

        val currentTrack = _uiState.value.playlist.getOrNull(index)
        _uiState.value = _uiState.value.copy(
            currentIndex = index,
            currentTrack = currentTrack,
            positionMs = 0L,
        )
        publishSharedState()
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                if (playbackOwnerSource == EXTERNAL_SERVICE_SOURCE) {
                    delay(300)
                    continue
                }

                val index = player.currentMediaItemIndex
                val playlist = _uiState.value.playlist
                _uiState.value = _uiState.value.copy(
                    positionMs = player.currentPosition.coerceAtLeast(0L),
                    durationMs = if (player.duration > 0) player.duration else 0L,
                    currentIndex = if (index >= 0) index else _uiState.value.currentIndex,
                    currentTrack = playlist.getOrNull(index).takeIf { index >= 0 } ?: _uiState.value.currentTrack,
                )
                if (!isApplyingSharedState) publishSharedState()
                delay(300)
            }
        }
    }

    private fun observeSharedState() {
        viewModelScope.launch {
            SharedPlaybackStore.state.collect { sharedState ->
                if (sharedState.source == SYNC_SOURCE || sharedState.playlist.isEmpty()) return@collect
                applySharedState(sharedState)
            }
        }
    }

    private fun applySharedState(sharedState: SharedPlaybackState) {
        if (sharedState.source == EXTERNAL_SERVICE_SOURCE) {
            applyExternalSharedState(sharedState)
            return
        }

        playbackOwnerSource = SYNC_SOURCE
        isApplyingSharedState = true
        try {
            val currentTrack = sharedState.playlist.getOrNull(sharedState.currentIndex)

            val incomingPlayableTracks = sharedState.playlist.filter { !it.previewUrl.isNullOrBlank() }
            val shouldReplacePlaylist =
                player.mediaItemCount != incomingPlayableTracks.size || _uiState.value.playlist.map { it.trackId } != sharedState.playlist.map { it.trackId }

            if (shouldReplacePlaylist) {
                player.stop()
                player.clearMediaItems()
                incomingPlayableTracks.forEach { track ->
                    track.previewUrl?.let { url -> player.addMediaItem(MediaItem.fromUri(url)) }
                }
                if (player.mediaItemCount > 0) {
                    player.prepare()
                }
            }

            if (player.mediaItemCount > 0) {
                val safeIndex = sharedState.currentIndex.coerceIn(0, player.mediaItemCount - 1)
                player.seekTo(safeIndex, sharedState.positionMs.coerceAtLeast(0L))
                player.playWhenReady = sharedState.isPlaying
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = null,
                albumTitle = currentTrack?.collectionName ?: _uiState.value.albumTitle,
                currentTrack = currentTrack,
                playlist = sharedState.playlist,
                currentIndex = sharedState.currentIndex.coerceAtLeast(0),
                isPlaying = sharedState.isPlaying,
                isLoopEnabled = sharedState.isLoopEnabled,
                durationMs = sharedState.durationMs,
                positionMs = sharedState.positionMs,
            )

            player.repeatMode = if (sharedState.isLoopEnabled) {
                Player.REPEAT_MODE_ONE
            } else {
                Player.REPEAT_MODE_OFF
            }
        } finally {
            isApplyingSharedState = false
        }
    }

    private fun applyExternalSharedState(sharedState: SharedPlaybackState) {
        playbackOwnerSource = EXTERNAL_SERVICE_SOURCE
        isApplyingSharedState = true
        try {
            if (player.isPlaying || player.playWhenReady || player.mediaItemCount > 0) {
                player.stop()
                player.clearMediaItems()
            }

            val currentTrack = sharedState.playlist.getOrNull(sharedState.currentIndex)
            loadedTrackId = currentTrack?.trackId
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = null,
                albumTitle = currentTrack?.collectionName ?: _uiState.value.albumTitle,
                currentTrack = currentTrack,
                playlist = sharedState.playlist,
                currentIndex = sharedState.currentIndex.coerceAtLeast(0),
                isPlaying = sharedState.isPlaying,
                isLoopEnabled = sharedState.isLoopEnabled,
                durationMs = sharedState.durationMs,
                positionMs = sharedState.positionMs,
            )
        } finally {
            isApplyingSharedState = false
        }
    }

    private fun publishSharedState() {
        if (isApplyingSharedState) return
        val state = _uiState.value
        SharedPlaybackStore.publish(
            source = SYNC_SOURCE,
            playlist = state.playlist,
            currentIndex = state.currentIndex,
            isPlaying = state.isPlaying,
            isLoopEnabled = state.isLoopEnabled,
            positionMs = state.positionMs,
            durationMs = state.durationMs,
            query = SharedPlaybackStore.state.value.query,
        )
    }

    override fun onCleared() {
        progressJob?.cancel()
        player.release()
        super.onCleared()
    }
}
