package dev.jmoicano.multiplayer.appauto.playback

import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.player.PlaylistNavigator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistNavigatorTest {

    private val tracks = listOf(
        Track(trackId = 1L, trackName = "Track 1", artistName = "Artist", previewUrl = "https://a"),
        Track(trackId = 2L, trackName = "Track 2", artistName = "Artist", previewUrl = "https://b"),
        Track(trackId = 3L, trackName = "Track 3", artistName = "Artist", previewUrl = "https://c"),
    )

    @Test
    fun `firstOrNull selects first track`() {
        val navigator = PlaylistNavigator()
        navigator.setPlaylist(tracks)

        val selected = navigator.firstOrNull()

        assertEquals(1L, selected?.trackId)
        assertEquals(1L, navigator.currentOrNull()?.trackId)
    }

    @Test
    fun `nextOrNull advances until end of list`() {
        val navigator = PlaylistNavigator()
        navigator.setPlaylist(tracks)
        navigator.firstOrNull()

        val second = navigator.nextOrNull()
        val third = navigator.nextOrNull()
        val afterEnd = navigator.nextOrNull()

        assertEquals(2L, second?.trackId)
        assertEquals(3L, third?.trackId)
        assertNull(afterEnd)
        assertEquals(3L, navigator.currentOrNull()?.trackId)
    }

    @Test
    fun `selectByMediaId updates current track`() {
        val navigator = PlaylistNavigator()
        navigator.setPlaylist(tracks)

        val selected = navigator.selectByMediaId("2")

        assertEquals(2L, selected?.trackId)
        assertEquals(2L, navigator.currentOrNull()?.trackId)
    }
}

