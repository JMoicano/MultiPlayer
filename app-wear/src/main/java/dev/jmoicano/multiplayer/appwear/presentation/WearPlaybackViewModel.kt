package dev.jmoicano.multiplayer.appwear.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.jmoicano.multiplayer.appwear.presentation.sync.WearCommandSender
import dev.jmoicano.multiplayer.appwear.presentation.sync.WearPlaybackSyncStore
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackCommand
import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WearPlaybackUiState(
    val currentTrack: Track? = null,
    val playlist: List<Track> = emptyList(),
    val browseTracks: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isLoopEnabled: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val query: String = "",
    val browseQuery: String = "",
)

@HiltViewModel
class WearPlaybackViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    companion object {
        private const val INITIAL_STATE_REQUEST_RETRIES = 4
        private const val INITIAL_STATE_REQUEST_INTERVAL_MS = 750L
        private const val LOG_TAG = "MPWearPlaybackVM"
    }

    private val _uiState = MutableStateFlow(WearPlaybackUiState())
    val uiState: StateFlow<WearPlaybackUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            WearPlaybackSyncStore.snapshot.collectLatest { snapshot ->
                val previousState = _uiState.value
                val playlist = if (snapshot.playlist.isNotEmpty()) snapshot.playlist else previousState.playlist
                val browseTracks = if (snapshot.browseTracks.isNotEmpty()) snapshot.browseTracks else previousState.browseTracks
                val currentIndex = when {
                    snapshot.currentIndex >= 0 -> snapshot.currentIndex
                    else -> previousState.currentIndex
                }

                _uiState.value = WearPlaybackUiState(
                    currentTrack = playlist.getOrNull(currentIndex),
                    playlist = playlist,
                    browseTracks = browseTracks,
                    currentIndex = currentIndex,
                    isPlaying = snapshot.isPlaying,
                    isLoopEnabled = snapshot.isLoopEnabled,
                    positionMs = snapshot.positionMs,
                    durationMs = snapshot.durationMs,
                    query = snapshot.query,
                    browseQuery = snapshot.browseQuery,
                )

                Log.d(
                    LOG_TAG,
                    "snapshot applied playlist=${playlist.size} browse=${browseTracks.size} index=$currentIndex currentTrackId=${_uiState.value.currentTrack?.trackId} isPlaying=${snapshot.isPlaying}",
                )
            }
        }
        requestInitialStateWithRetry()
    }

    fun requestState() {
        Log.d(LOG_TAG, "requestState")
        WearCommandSender.requestState(getApplication())
    }

    private fun requestInitialStateWithRetry() {
        viewModelScope.launch {
            repeat(INITIAL_STATE_REQUEST_RETRIES) {
                requestState()
                delay(INITIAL_STATE_REQUEST_INTERVAL_MS)
                if (hasReceivedSyncedState()) return@launch
            }
        }
    }

    private fun hasReceivedSyncedState(): Boolean {
        val snapshot = WearPlaybackSyncStore.snapshot.value
        return snapshot.updatedAtMs > 0L ||
            snapshot.browseUpdatedAtMs > 0L ||
            snapshot.playlist.isNotEmpty() ||
            snapshot.browseTracks.isNotEmpty()
    }

    fun togglePlayPause() {
        sendAction(CrossDevicePlaybackProtocol.ACTION_TOGGLE_PLAY_PAUSE)
        requestStateSoon()
    }

    fun playNext() {
        sendAction(CrossDevicePlaybackProtocol.ACTION_NEXT)
        requestStateSoon()
    }

    fun playPrevious() {
        sendAction(CrossDevicePlaybackProtocol.ACTION_PREVIOUS)
        requestStateSoon()
    }

    fun toggleLoop() {
        WearCommandSender.send(
            context = getApplication(),
            command = CrossDevicePlaybackCommand(
                action = CrossDevicePlaybackProtocol.ACTION_SET_LOOP,
                loopEnabled = !_uiState.value.isLoopEnabled,
            ),
        )
        requestStateSoon()
    }

    fun playTrack(trackId: Long) {
        WearCommandSender.send(
            context = getApplication(),
            command = CrossDevicePlaybackCommand(
                action = CrossDevicePlaybackProtocol.ACTION_PLAY_TRACK,
                trackId = trackId,
            ),
        )
        requestStateSoon()
    }

    private fun sendAction(action: String) {
        Log.d(LOG_TAG, "sendAction action=$action")
        WearCommandSender.send(
            context = getApplication(),
            command = CrossDevicePlaybackCommand(action = action),
        )
    }

    private fun requestStateSoon() {
        viewModelScope.launch {
            delay(200L)
            requestState()
        }
    }
}
