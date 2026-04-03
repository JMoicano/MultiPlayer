package dev.jmoicano.multiplayer.app.phone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import dev.jmoicano.multiplayer.app.phone.ui.splash.SplashScreen
import dev.jmoicano.multiplayer.feature.player.presentation.screens.PlayerScreen
import dev.jmoicano.multiplayer.feature.search.presentation.screens.AlbumScreen
import dev.jmoicano.multiplayer.feature.search.presentation.screens.SongsListScreen
import kotlinx.coroutines.delay

/**
 * Main navigation composable for the app.
 * Handles the transition from splash screen to songs list screen.
 */
@Composable
fun AppNavigation(
    onTrackClick: (Long) -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isSplashFinished = rememberSaveable { mutableStateOf(false) }
    val backStack = remember { mutableStateListOf<AppDestination>(AppDestination.SongsList) }

    fun navigateToPlayer(trackId: Long) {
        backStack.removeAll { it is AppDestination.Player }
        backStack.add(AppDestination.Player(trackId))
    }

    fun navigateToAlbum(trackId: Long) {
        backStack.removeAll { it is AppDestination.Album }
        backStack.add(AppDestination.Album(trackId))
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    LaunchedEffect(Unit) {
        // Show splash screen for a minimum duration
        delay(2000)
        isSplashFinished.value = true
    }

    if (!isSplashFinished.value) {
        SplashScreen()
    } else {
        when (val destination = backStack.last()) {
            AppDestination.SongsList -> {
                SongsListScreen(
                    isTabletLayout = isTabletLayout,
                    onTrackClick = { trackId ->
                        navigateToPlayer(trackId)
                        onTrackClick(trackId)
                    },
                    onViewAlbumClick = { trackId ->
                        navigateToAlbum(trackId)
                    },
                )
            }

            is AppDestination.Album -> {
                AlbumScreen(
                    trackId = destination.trackId,
                    isTabletLayout = isTabletLayout,
                    onBack = ::navigateBack,
                    onTrackClick = { trackId ->
                        navigateToPlayer(trackId)
                        onTrackClick(trackId)
                    },
                )
            }

            is AppDestination.Player -> {
                PlayerScreen(
                    trackId = destination.trackId,
                    isTabletLayout = isTabletLayout,
                    onBack = ::navigateBack,
                    onViewAlbumClick = { trackId ->
                        navigateToAlbum(trackId)
                    },
                )
            }
        }
    }
}

private sealed interface AppDestination {
    data object SongsList : AppDestination
    data class Album(val trackId: Long) : AppDestination
    data class Player(val trackId: Long) : AppDestination
}
