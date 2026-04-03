package dev.jmoicano.multiplayer.appwear.presentation.sync

import android.util.Log
import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Keeps the latest playback snapshot received from the phone in memory. */
object WearPlaybackSyncStore {
    private val _snapshot = MutableStateFlow(CrossDevicePlaybackSnapshot())

    /** Read-only flow of the snapshot shared with the watch UI. */
    val snapshot: StateFlow<CrossDevicePlaybackSnapshot> = _snapshot.asStateFlow()

    private const val LOG_TAG = "MPWearSyncStore"

    /** Updates the snapshot consumed by the watch UI. */
    fun update(newSnapshot: CrossDevicePlaybackSnapshot) {
        Log.d(
            LOG_TAG,
            "update playlist=${newSnapshot.playlist.size} browse=${newSnapshot.browseTracks.size} index=${newSnapshot.currentIndex} updatedAt=${newSnapshot.updatedAtMs} browseUpdatedAt=${newSnapshot.browseUpdatedAtMs}",
        )
        _snapshot.value = newSnapshot
    }
}
