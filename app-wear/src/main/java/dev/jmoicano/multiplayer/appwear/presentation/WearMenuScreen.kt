package dev.jmoicano.multiplayer.appwear.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ScalingLazyColumn
import dev.jmoicano.multiplayer.core.designsystem.R

@Composable
fun WearMenuScreen(
    onOpenNowPlaying: () -> Unit,
    onOpenAlbums: () -> Unit,
    onOpenSongs: () -> Unit,
) {
    ScalingLazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            WearScreenTitle(
                primary = stringResource(dev.jmoicano.multiplayer.app.wear.R.string.wear_menu_title),
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item {
            WearMenuNavItem(
                label = stringResource(R.string.screen_title_now_playing),
                iconRes = R.drawable.ic_play,
                onClick = onOpenNowPlaying,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
        item {
            WearMenuNavItem(
                label = stringResource(dev.jmoicano.multiplayer.app.wear.R.string.wear_menu_albums),
                iconRes = R.drawable.ic_cd,
                onClick = onOpenAlbums,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
        item {
            WearMenuNavItem(
                label = stringResource(R.string.screen_title_songs),
                iconRes = R.drawable.ic_music_list,
                onClick = onOpenSongs,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}
