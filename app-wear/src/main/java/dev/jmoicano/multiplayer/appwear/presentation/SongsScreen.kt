package dev.jmoicano.multiplayer.appwear.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ScalingLazyColumn
import dev.jmoicano.multiplayer.core.network.model.Track

@Composable
fun SongsScreen(
    tracks: List<Track>,
    currentTrackId: Long?,
    onBack: () -> Unit,
    onPlayTrack: (Track) -> Unit,
) {
    ScalingLazyColumn(modifier = Modifier.fillMaxSize()) {
        tracks.forEach { track ->
            item {
                WearMenuTrackItem(
                    trackName = track.trackName,
                    artistName = track.artistName,
                    artworkUrl = track.artworkUrl100,
                    isActive = track.trackId == currentTrackId,
                    onClick = { onPlayTrack(track) },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}
