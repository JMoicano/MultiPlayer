package dev.jmoicano.multiplayer.core.database.mapper

import dev.jmoicano.multiplayer.core.network.model.Track
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackEntityMapperTest {

    @Test
    fun `toEntity and toTrack keep all fields`() {
        val track = Track(
            wrapperType = "track",
            kind = "song",
            collectionId = 123L,
            trackId = 456L,
            artistName = "Artist",
            collectionName = "Collection",
            trackName = "Track",
            collectionCensoredName = "Collection Censored",
            trackCensoredName = "Track Censored",
            artistViewUrl = "artist-url",
            collectionViewUrl = "collection-url",
            trackViewUrl = "track-url",
            previewUrl = "preview-url",
            artworkUrl30 = "artwork-30",
            artworkUrl60 = "artwork-60",
            artworkUrl100 = "artwork-100",
            collectionPrice = 10.5,
            trackPrice = 1.99,
            releaseDate = "2026-01-01T00:00:00Z",
            collectionExplicitness = "notExplicit",
            trackExplicitness = "notExplicit",
            discNumber = 1,
            trackNumber = 3,
            trackTimeMillis = 180000L,
            country = "BR",
            currency = "BRL",
            primaryGenreName = "Pop",
            isStreamable = true,
            hasLyrics = true,
        )

        val mappedBack = track.toEntity().toTrack()

        assertEquals(track, mappedBack)
    }
}

