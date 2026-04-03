package dev.jmoicano.multiplayer.app.phone.auto

import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.player.PlaylistNavigator
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PlaylistNavigator
 */
class PlaylistNavigatorTest {

    private lateinit var navigator: PlaylistNavigator
    private lateinit var sampleTracks: List<Track>

    @Before
    fun setUp() {
        navigator = PlaylistNavigator()
        sampleTracks = listOf(
            Track(
                trackId = 1,
                trackName = "Song 1",
                artistName = "Artist A",
                previewUrl = "https://example.com/1.mp3",
                artworkUrl100 = "https://example.com/1.jpg",
                trackTimeMillis = 180000
            ),
            Track(
                trackId = 2,
                trackName = "Song 2",
                artistName = "Artist B",
                previewUrl = "https://example.com/2.mp3",
                artworkUrl100 = "https://example.com/2.jpg",
                trackTimeMillis = 200000
            ),
            Track(
                trackId = 3,
                trackName = "Song 3",
                artistName = "Artist C",
                previewUrl = "https://example.com/3.mp3",
                artworkUrl100 = "https://example.com/3.jpg",
                trackTimeMillis = 220000
            )
        )
    }

    @Test
    fun testEmptyPlaylist() {
        assert(navigator.firstOrNull() == null)
        assert(navigator.currentOrNull() == null)
        assert(navigator.nextOrNull() == null)
        assert(navigator.previousOrNull() == null)
    }

    @Test
    fun testSetPlaylist() {
        navigator.setPlaylist(sampleTracks)
        assert(navigator.currentOrNull() != null)
    }

    @Test
    fun testFirstTrack() {
        navigator.setPlaylist(sampleTracks)
        val first = navigator.firstOrNull()
        assert(first?.trackId == 1L)
        assert(first?.trackName == "Song 1")
        assert(navigator.currentOrNull()?.trackId == 1L)
    }

    @Test
    fun testNavigateNext() {
        navigator.setPlaylist(sampleTracks)
        navigator.firstOrNull()

        val second = navigator.nextOrNull()
        assert(second?.trackId == 2L)

        val third = navigator.nextOrNull()
        assert(third?.trackId == 3L)

        val outOfBounds = navigator.nextOrNull()
        assert(outOfBounds == null)
    }

    @Test
    fun testNavigatePrevious() {
        navigator.setPlaylist(sampleTracks)
        navigator.firstOrNull()
        navigator.nextOrNull()
        navigator.nextOrNull()

        val previous = navigator.previousOrNull()
        assert(previous?.trackId == 2L)

        val first = navigator.previousOrNull()
        assert(first?.trackId == 1L)

        val outOfBounds = navigator.previousOrNull()
        assert(outOfBounds == null)
    }

    @Test
    fun testSelectByMediaId() {
        navigator.setPlaylist(sampleTracks)

        val selected = navigator.selectByMediaId("2")
        assert(selected?.trackId == 2L)
        assert(navigator.currentOrNull()?.trackId == 2L)
    }

    @Test
    fun testSelectByInvalidMediaId() {
        navigator.setPlaylist(sampleTracks)

        val selected = navigator.selectByMediaId("999")
        assert(selected == null)
    }

    @Test
    fun testPlaylistUpdate() {
        navigator.setPlaylist(sampleTracks)
        navigator.firstOrNull()
        navigator.nextOrNull()

        // Update playlist with a smaller list
        val smallerPlaylist = sampleTracks.take(1)
        navigator.setPlaylist(smallerPlaylist)

        // Current index should be adjusted
        assert(navigator.currentOrNull() != null)
        assert(navigator.nextOrNull() == null)
    }
}

