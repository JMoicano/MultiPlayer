package dev.jmoicano.multiplayer.appwear.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.jmoicano.multiplayer.appwear.presentation.theme.MultiPlayerTheme
import kotlinx.coroutines.delay

@Composable
fun WearApp(viewModel: WearPlaybackViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    var screen by remember { mutableStateOf(WearScreen.Menu) }
    val uiState by viewModel.uiState.collectAsState()
    val libraryTracks = if (uiState.browseTracks.isNotEmpty()) uiState.browseTracks else uiState.playlist

    MultiPlayerTheme {
        if (showSplash) {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(2000)
                showSplash = false
                viewModel.requestState()
            }
        } else {
            when (screen) {
                WearScreen.Menu -> WearMenuScreen(
                    onOpenNowPlaying = {
                        viewModel.requestState()
                        screen = WearScreen.NowPlaying
                    },
                    onOpenAlbums = {
                        viewModel.requestState()
                        screen = WearScreen.Albums
                    },
                    onOpenSongs = {
                        viewModel.requestState()
                        screen = WearScreen.Songs
                    },
                )

                WearScreen.NowPlaying -> NowPlayingScreen(
                    state = uiState,
                    onSwipeDownToMenu = { screen = WearScreen.Menu },
                    onPrevious = viewModel::playPrevious,
                    onPlayPause = viewModel::togglePlayPause,
                    onNext = viewModel::playNext,
                    onToggleLoop = viewModel::toggleLoop,
                )

                WearScreen.Albums -> AlbumsScreen(
                    tracks = libraryTracks,
                    currentTrackId = uiState.currentTrack?.trackId,
                    onBack = { screen = WearScreen.Menu },
                    onPlayAlbum = { track ->
                        viewModel.playTrack(track.trackId)
                        screen = WearScreen.NowPlaying
                    },
                )

                WearScreen.Songs -> SongsScreen(
                    tracks = libraryTracks,
                    currentTrackId = uiState.currentTrack?.trackId,
                    onBack = { screen = WearScreen.Menu },
                    onPlayTrack = { track ->
                        viewModel.playTrack(track.trackId)
                        screen = WearScreen.NowPlaying
                    },
                )
            }
        }
    }
}

private enum class WearScreen {
    Menu,
    NowPlaying,
    Albums,
    Songs,
}

