package dev.jmoicano.multiplayer.core.player.sync

import dev.jmoicano.multiplayer.core.network.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedPlaybackStoreTest {

    @Test
    fun publish_updatesStateWithPlaylistAndPlaybackFields() {
        val playlist = listOf(
            Track(trackId = 1L, trackName = "A", artistName = "Artist"),
            Track(trackId = 2L, trackName = "B", artistName = "Artist"),
        )

        SharedPlaybackStore.publish(
            source = "test",
            playlist = playlist,
            currentIndex = 1,
            isPlaying = true,
            isLoopEnabled = true,
            positionMs = 5000L,
            durationMs = 30000L,
            query = "Beatles",
        )

        val state = SharedPlaybackStore.state.value
        assertEquals(2, state.playlist.size)
        assertEquals(1, state.currentIndex)
        assertEquals("B", state.currentTrack?.trackName)
        assertTrue(state.isPlaying)
        assertTrue(state.isLoopEnabled)
        assertEquals(5000L, state.positionMs)
        assertEquals(30000L, state.durationMs)
        assertEquals("Beatles", state.query)
    }

    @Test
    fun publish_coercesNegativePositionAndInvalidIndex() {
        val playlist = listOf(Track(trackId = 1L, trackName = "A", artistName = "Artist"))

        SharedPlaybackStore.publish(
            source = "test",
            playlist = playlist,
            currentIndex = 9,
            isPlaying = false,
            isLoopEnabled = false,
            positionMs = -10L,
            durationMs = -30L,
        )

        val state = SharedPlaybackStore.state.value
        assertEquals(0, state.currentIndex)
        assertFalse(state.isPlaying)
        assertEquals(0L, state.positionMs)
        assertEquals(0L, state.durationMs)
    }

    @Test
    fun publishBrowseSnapshot_updatesBrowseFieldsWithoutDroppingPlaybackState() {
        val playlist = listOf(Track(trackId = 10L, trackName = "Now", artistName = "Artist"))
        SharedPlaybackStore.publish(
            source = "playback",
            playlist = playlist,
            currentIndex = 0,
            isPlaying = true,
            positionMs = 1000L,
            durationMs = 30000L,
            query = "Beatles",
        )

        val browseTracks = listOf(
            Track(trackId = 1L, trackName = "Search A", artistName = "Artist A"),
            Track(trackId = 2L, trackName = "Search B", artistName = "Artist B"),
        )
        SharedPlaybackStore.publishBrowseSnapshot(
            tracks = browseTracks,
            query = "Queen",
        )

        val state = SharedPlaybackStore.state.value
        assertEquals(1, state.playlist.size)
        assertTrue(state.isPlaying)
        assertEquals(2, state.browseTracks.size)
        assertEquals("Search A", state.browseTracks.first().trackName)
        assertEquals("Queen", state.browseQuery)
    }
}

