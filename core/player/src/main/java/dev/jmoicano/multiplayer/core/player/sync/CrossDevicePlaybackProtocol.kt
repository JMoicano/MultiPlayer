package dev.jmoicano.multiplayer.core.player.sync

import dev.jmoicano.multiplayer.core.network.model.Track
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Shared protocol between phone and Wear OS over the Wear Data Layer.
 *
 * [CrossDevicePlaybackSnapshot] and [CrossDevicePlaybackCommand] are both fully
 * serialisable via kotlinx.serialization, which allows round-trip tests to run on
 * the JVM without any Android-specific mocking.
 */
/** Serializable snapshot of playback state shared across devices. */
@Serializable
data class CrossDevicePlaybackSnapshot(
    val playlist: List<Track> = emptyList(),
    val browseTracks: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isLoopEnabled: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val query: String = "",
    val browseQuery: String = "",
    val updatedAtMs: Long = 0L,
    val browseUpdatedAtMs: Long = 0L,
)

/** Serializable command sent from watch to phone. */
@Serializable
data class CrossDevicePlaybackCommand(
    val action: String,
    val trackId: Long? = null,
    val positionMs: Long? = null,
    val loopEnabled: Boolean? = null,
)

/** Defines paths, actions, and serialization used by Wear Data Layer sync. */
object CrossDevicePlaybackProtocol {
    const val STATE_DATA_PATH = "/multiplayer/playback/state"
    const val COMMAND_MESSAGE_PATH = "/multiplayer/playback/command"

    const val ACTION_TOGGLE_PLAY_PAUSE = "toggle_play_pause"
    const val ACTION_PLAY = "play"
    const val ACTION_PAUSE = "pause"
    const val ACTION_NEXT = "next"
    const val ACTION_PREVIOUS = "previous"
    const val ACTION_PLAY_TRACK = "play_track"
    const val ACTION_SEEK_TO = "seek_to"
    const val ACTION_SET_LOOP = "set_loop"
    const val ACTION_REQUEST_STATE = "request_state"

    private const val MAX_SYNC_TRACKS_PER_LIST = 20

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    /** Converts phone shared state into a compact snapshot for synchronization. */
    fun toSnapshot(state: SharedPlaybackState): CrossDevicePlaybackSnapshot {
        val syncedPlaylist = state.playlist.toSyncPlaylist(
            currentIndex = state.currentIndex,
            maxSize = MAX_SYNC_TRACKS_PER_LIST,
        )

        return CrossDevicePlaybackSnapshot(
            playlist = syncedPlaylist.tracks,
            browseTracks = state.browseTracks.take(MAX_SYNC_TRACKS_PER_LIST),
            currentIndex = syncedPlaylist.currentIndex,
            isPlaying = state.isPlaying,
            isLoopEnabled = state.isLoopEnabled,
            positionMs = state.positionMs,
            durationMs = state.durationMs,
            query = state.query,
            browseQuery = state.browseQuery,
            updatedAtMs = state.updatedAtMs,
            browseUpdatedAtMs = state.browseUpdatedAtMs,
        )
    }

    /** Serialises [snapshot] to a JSON string. */
    fun snapshotToJson(snapshot: CrossDevicePlaybackSnapshot): String =
        json.encodeToString(snapshot)

    /** Deserialises a JSON string back into [CrossDevicePlaybackSnapshot], or `null` on failure. */
    fun snapshotFromJson(payload: String): CrossDevicePlaybackSnapshot? =
        runCatching { json.decodeFromString<CrossDevicePlaybackSnapshot>(payload) }.getOrNull()

    /** Serialises [command] to a UTF-8 encoded JSON payload. */
    fun commandToPayload(command: CrossDevicePlaybackCommand): ByteArray =
        json.encodeToString(command).encodeToByteArray()

    /** Deserialises a UTF-8 encoded JSON payload back into [CrossDevicePlaybackCommand], or `null` on failure. */
    fun commandFromPayload(payload: ByteArray): CrossDevicePlaybackCommand? =
        runCatching { json.decodeFromString<CrossDevicePlaybackCommand>(payload.decodeToString()) }.getOrNull()

    private data class SyncPlaylist(
        val tracks: List<Track>,
        val currentIndex: Int,
    )

    private fun List<Track>.toSyncPlaylist(currentIndex: Int, maxSize: Int): SyncPlaylist {
        if (isEmpty()) return SyncPlaylist(emptyList(), -1)

        val validCurrentIndex = currentIndex.takeIf { it in indices } ?: -1
        if (size <= maxSize) {
            return SyncPlaylist(
                tracks = this,
                currentIndex = validCurrentIndex,
            )
        }

        if (validCurrentIndex < 0) {
            return SyncPlaylist(
                tracks = take(maxSize),
                currentIndex = -1,
            )
        }

        val start = (validCurrentIndex - (maxSize / 2)).coerceIn(0, size - maxSize)
        val endExclusive = start + maxSize
        return SyncPlaylist(
            tracks = subList(start, endExclusive),
            currentIndex = validCurrentIndex - start,
        )
    }
}
