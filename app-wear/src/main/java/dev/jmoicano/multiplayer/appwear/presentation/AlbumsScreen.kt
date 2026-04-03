package dev.jmoicano.multiplayer.appwear.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ScalingLazyColumn
import dev.jmoicano.multiplayer.core.network.model.Track

@Composable
fun AlbumsScreen(
    tracks: List<Track>,
    currentTrackId: Long?,
    onBack: () -> Unit,
    onPlayAlbum: (Track) -> Unit,
) {
    val unknownAlbum = stringResource(dev.jmoicano.multiplayer.app.wear.R.string.wear_unknown_album)
    val albums = tracks
        .groupBy { it.collectionName ?: unknownAlbum }
        .entries
        .sortedBy { it.key }

    val currentTrack = tracks.firstOrNull { it.trackId == currentTrackId }
    val titleAlbum = currentTrack?.collectionName
        ?: stringResource(dev.jmoicano.multiplayer.app.wear.R.string.wear_menu_albums)
    val titleArtist = currentTrack?.artistName

    ScalingLazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            WearScreenTitle(
                primary = titleAlbum,
                secondary = titleArtist,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        albums.forEach { album ->
            item {
                val activeTrack = album.value.firstOrNull { it.trackId == currentTrackId }
                    ?: album.value.firstOrNull()
                val albumArtwork = activeTrack?.artworkUrl100
                val albumArtist = album.value.firstOrNull()?.artistName.orEmpty()
                val isActive = album.value.any { it.trackId == currentTrackId }

                WearMenuTrackItem(
                    trackName = album.key,
                    artistName = albumArtist,
                    artworkUrl = albumArtwork,
                    isActive = isActive,
                    onClick = { activeTrack?.let(onPlayAlbum) },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}
