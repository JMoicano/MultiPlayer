package dev.jmoicano.multiplayer.core.player

import dev.jmoicano.multiplayer.core.network.model.Track

/**
 * Keeps playlist navigation state isolated from any specific Android component.
 *
 * Maintains the current playback position within a [Track] list and exposes
 * cursor-style accessors ([currentOrNull], [nextOrNull], [previousOrNull]) and
 * direct seek helpers ([selectByMediaId], [selectByIndex]).
 *
 * This class is intentionally free from Android framework dependencies so it
 * can be unit-tested without instrumentation.
 */
class PlaylistNavigator {

    private var playlist: List<Track> = emptyList()
    private var currentIndex: Int = -1

    /**
     * Replaces the current playlist. The cursor position is clamped to the new
     * list bounds (or reset to -1 if the new list is empty).
     */
    fun setPlaylist(items: List<Track>) {
        playlist = items
        currentIndex = if (playlist.isEmpty()) {
            -1
        } else {
            currentIndex.coerceIn(0, playlist.lastIndex)
        }
    }

    /** Returns the track at the current cursor position, or `null` if none. */
    fun currentOrNull(): Track? = playlist.getOrNull(currentIndex)

    /** Returns the current cursor index (may be -1 if the playlist is empty). */
    fun currentIndex(): Int = currentIndex

    /**
     * Moves the cursor to the first track and returns it, or `null` if the
     * playlist is empty.
     */
    fun firstOrNull(): Track? {
        if (playlist.isEmpty()) return null
        currentIndex = 0
        return playlist[currentIndex]
    }

    /**
     * Finds the track matching [mediaId] (compared against [Track.trackId]),
     * moves the cursor to it, and returns it. Returns `null` if not found.
     */
    fun selectByMediaId(mediaId: String): Track? {
        val targetIndex = playlist.indexOfFirst { it.trackId.toString() == mediaId }
        if (targetIndex < 0) return null
        currentIndex = targetIndex
        return playlist[currentIndex]
    }

    /**
     * Moves the cursor to [index] and returns the track there.
     * Returns `null` if [index] is out of bounds.
     */
    fun selectByIndex(index: Int): Track? {
        if (index !in playlist.indices) return null
        currentIndex = index
        return playlist[currentIndex]
    }

    /**
     * Advances the cursor by one and returns the next track, or `null` if
     * already at the end of the playlist.
     */
    fun nextOrNull(): Track? {
        if (playlist.isEmpty()) return null
        if (currentIndex < 0) {
            currentIndex = 0
            return playlist[currentIndex]
        }
        if (currentIndex >= playlist.lastIndex) return null
        currentIndex += 1
        return playlist[currentIndex]
    }

    /**
     * Moves the cursor back by one and returns the previous track, or `null`
     * if already at the start of the playlist.
     */
    fun previousOrNull(): Track? {
        if (playlist.isEmpty()) return null
        if (currentIndex <= 0) return null
        currentIndex -= 1
        return playlist[currentIndex]
    }
}

