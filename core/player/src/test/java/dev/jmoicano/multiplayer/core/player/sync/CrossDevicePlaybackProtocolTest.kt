package dev.jmoicano.multiplayer.core.player.sync

import dev.jmoicano.multiplayer.core.network.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrossDevicePlaybackProtocolTest {

    @Test
    fun snapshot_jsonRoundTrip_keepsCorePlaybackFields() {
        val snapshot = CrossDevicePlaybackSnapshot(
            playlist = listOf(
                Track(trackId = 1L, trackName = "Song A", artistName = "Artist A"),
                Track(trackId = 2L, trackName = "Song B", artistName = "Artist B"),
            ),
            browseTracks = listOf(
                Track(trackId = 10L, trackName = "Browse A", artistName = "Artist X"),
            ),
            currentIndex = 1,
            isPlaying = true,
            isLoopEnabled = true,
            positionMs = 1200L,
            durationMs = 30_000L,
            query = "ed sheeran",
            browseQuery = "beatles",
            updatedAtMs = 999L,
            browseUpdatedAtMs = 555L,
        )

        val encoded = CrossDevicePlaybackProtocol.snapshotToJson(snapshot)
        val decoded = CrossDevicePlaybackProtocol.snapshotFromJson(encoded)

        assertNotNull(decoded)
        assertEquals(2, decoded?.playlist?.size)
        assertEquals(1, decoded?.currentIndex)
        assertEquals(true, decoded?.isPlaying)
        assertEquals(true, decoded?.isLoopEnabled)
        assertEquals(1200L, decoded?.positionMs)
        assertEquals(30_000L, decoded?.durationMs)
        assertEquals("Song B", decoded?.playlist?.get(1)?.trackName)
        assertEquals(1, decoded?.browseTracks?.size)
        assertEquals("Browse A", decoded?.browseTracks?.firstOrNull()?.trackName)
        assertEquals("beatles", decoded?.browseQuery)
        assertEquals(555L, decoded?.browseUpdatedAtMs)
    }

    @Test
    fun command_payloadRoundTrip_keepsActionAndArguments() {
        val command = CrossDevicePlaybackCommand(
            action = CrossDevicePlaybackProtocol.ACTION_PLAY_TRACK,
            trackId = 42L,
            positionMs = 5000L,
            loopEnabled = false,
        )

        val payload = CrossDevicePlaybackProtocol.commandToPayload(command)
        val decoded = CrossDevicePlaybackProtocol.commandFromPayload(payload)

        assertNotNull(decoded)
        assertEquals(CrossDevicePlaybackProtocol.ACTION_PLAY_TRACK, decoded?.action)
        assertEquals(42L, decoded?.trackId)
        assertEquals(5000L, decoded?.positionMs)
        assertEquals(false, decoded?.loopEnabled)
    }

    @Test
    fun invalidPayload_returnsNull() {
        val decoded = CrossDevicePlaybackProtocol.commandFromPayload("not-json".encodeToByteArray())
        assertTrue(decoded == null)
    }

    @Test
    fun toSnapshot_limitsPlaylistAndKeepsCurrentTrackInWindow() {
        val playlist = (0 until 60).map { index ->
            Track(
                trackId = index.toLong(),
                trackName = "Song $index",
                artistName = "Artist $index",
            )
        }
        val state = SharedPlaybackState(
            playlist = playlist,
            currentIndex = 45,
            isPlaying = true,
        )

        val snapshot = CrossDevicePlaybackProtocol.toSnapshot(state)

        assertEquals(20, snapshot.playlist.size)
        assertTrue(snapshot.currentIndex in snapshot.playlist.indices)
        assertEquals(45L, snapshot.playlist[snapshot.currentIndex].trackId)
    }

    @Test
    fun toSnapshot_limitsBrowseTracksSize() {
        val browseTracks = (0 until 45).map { index ->
            Track(
                trackId = (index + 1000).toLong(),
                trackName = "Browse $index",
                artistName = "Artist $index",
            )
        }
        val state = SharedPlaybackState(
            browseTracks = browseTracks,
            browseQuery = "rock",
        )

        val snapshot = CrossDevicePlaybackProtocol.toSnapshot(state)

        assertEquals(20, snapshot.browseTracks.size)
        assertEquals(1000L, snapshot.browseTracks.first().trackId)
        assertEquals(1019L, snapshot.browseTracks.last().trackId)
    }
}
