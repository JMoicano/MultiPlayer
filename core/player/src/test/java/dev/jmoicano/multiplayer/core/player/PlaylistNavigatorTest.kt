package dev.jmoicano.multiplayer.core.player

import dev.jmoicano.multiplayer.core.network.model.Track
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PlaylistNavigatorTest {

    private lateinit var navigator: PlaylistNavigator

    private val track1 = Track(trackId = 1L, trackName = "Song 1", artistName = "Artist A")
    private val track2 = Track(trackId = 2L, trackName = "Song 2", artistName = "Artist B")
    private val track3 = Track(trackId = 3L, trackName = "Song 3", artistName = "Artist C")
    private val playlist = listOf(track1, track2, track3)

    @Before
    fun setUp() {
        navigator = PlaylistNavigator()
    }

    @Test
    fun emptyPlaylist_currentOrNull_returnsNull() {
        assertNull(navigator.currentOrNull())
    }

    @Test
    fun setPlaylist_firstOrNull_returnsFirstTrack() {
        navigator.setPlaylist(playlist)
        val result = navigator.firstOrNull()
        assertEquals(track1, result)
        assertEquals(0, navigator.currentIndex())
    }

    @Test
    fun nextOrNull_advancesThroughPlaylist() {
        navigator.setPlaylist(playlist)
        navigator.firstOrNull()

        assertEquals(track2, navigator.nextOrNull())
        assertEquals(track3, navigator.nextOrNull())
    }

    @Test
    fun nextOrNull_atEnd_returnsNull() {
        navigator.setPlaylist(playlist)
        navigator.selectByIndex(playlist.lastIndex)

        assertNull(navigator.nextOrNull())
    }

    @Test
    fun previousOrNull_atStart_returnsNull() {
        navigator.setPlaylist(playlist)
        navigator.firstOrNull()

        assertNull(navigator.previousOrNull())
    }

    @Test
    fun previousOrNull_movesBackward() {
        navigator.setPlaylist(playlist)
        navigator.selectByIndex(2)

        assertEquals(track2, navigator.previousOrNull())
        assertEquals(track1, navigator.previousOrNull())
    }

    @Test
    fun selectByMediaId_withValidId_returnCorrectTrack() {
        navigator.setPlaylist(playlist)

        val result = navigator.selectByMediaId("2")

        assertEquals(track2, result)
        assertEquals(1, navigator.currentIndex())
    }

    @Test
    fun selectByMediaId_withInvalidId_returnsNull() {
        navigator.setPlaylist(playlist)

        assertNull(navigator.selectByMediaId("999"))
    }

    @Test
    fun selectByIndex_withValidIndex_returnsCorrectTrack() {
        navigator.setPlaylist(playlist)

        assertEquals(track3, navigator.selectByIndex(2))
    }

    @Test
    fun selectByIndex_withOutOfBoundsIndex_returnsNull() {
        navigator.setPlaylist(playlist)

        assertNull(navigator.selectByIndex(10))
    }

    @Test
    fun setPlaylist_empty_resetsIndexToMinusOne() {
        navigator.setPlaylist(playlist)
        navigator.firstOrNull()

        navigator.setPlaylist(emptyList())

        assertEquals(-1, navigator.currentIndex())
        assertNull(navigator.currentOrNull())
    }
}

