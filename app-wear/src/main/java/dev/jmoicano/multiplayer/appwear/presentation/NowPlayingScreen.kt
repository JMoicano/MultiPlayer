package dev.jmoicano.multiplayer.appwear.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import coil.compose.AsyncImage
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem

@Composable
fun NowPlayingScreen(
    state: WearPlaybackUiState,
    onSwipeDownToMenu: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onToggleLoop: () -> Unit,
) {
    var draggedDistancePx by remember { mutableFloatStateOf(0f) }
    val durationMs = state.durationMs.coerceAtLeast(0L)
    val clampedPosition = state.positionMs.coerceIn(0L, durationMs.takeIf { it > 0L } ?: 0L)
    val progress = if (durationMs > 0L) clampedPosition.toFloat() / durationMs.toFloat() else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0f) {
                            draggedDistancePx += dragAmount
                            if (draggedDistancePx > 120f) {
                                draggedDistancePx = 0f
                                onSwipeDownToMenu()
                            }
                        }
                    },
                    onDragEnd = { draggedDistancePx = 0f },
                    onDragCancel = { draggedDistancePx = 0f },
                )
            },
    ) {
        AsyncImage(
            model = state.currentTrack?.artworkUrl100,
            contentDescription = state.currentTrack?.trackName,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = dev.jmoicano.multiplayer.core.designsystem.R.drawable.ic_musical_note),
            error = painterResource(id = dev.jmoicano.multiplayer.core.designsystem.R.drawable.ic_musical_note),
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DesignSystem.colors.wearOverlay),
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = state.currentTrack?.trackName
                        ?: stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.wear_no_track),
                    style = DesignSystem.typography.titleMedium.copy(
                        color = DesignSystem.colors.basePrimaryWhite,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.currentTrack?.artistName
                        ?: stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.wear_waiting_for_phone),
                    style = DesignSystem.typography.bodyMedium.copy(
                        color = DesignSystem.colors.wearTextSecondary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clickable(onClick = onPrevious),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = DesignSystem.icons.forwardBar),
                            contentDescription = stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.wear_previous),
                            tint = DesignSystem.colors.basePrimaryWhite,
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(180f),
                        )
                    }

                    Box(
                        modifier = Modifier.size(62.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxSize(),
                            indicatorColor = DesignSystem.colors.basePrimaryWhite,
                            trackColor = DesignSystem.colors.wearProgressTrack,
                            strokeWidth = 3.dp,
                        )
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clickable(onClick = onPlayPause),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (state.isPlaying) DesignSystem.icons.pause else DesignSystem.icons.play,
                                ),
                                contentDescription = if (state.isPlaying) {
                                    stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.wear_pause)
                                } else {
                                    stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.wear_play)
                                },
                                tint = DesignSystem.colors.basePrimaryWhite,
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clickable(onClick = onNext),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = DesignSystem.icons.forwardBar),
                            contentDescription = stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.wear_next),
                            tint = DesignSystem.colors.basePrimaryWhite,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clickable(onClick = onToggleLoop),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = DesignSystem.icons.playOnRepeat),
                        contentDescription = if (state.isLoopEnabled) {
                            stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.wear_disable_loop)
                        } else {
                            stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.wear_enable_loop)
                        },
                        tint = if (state.isLoopEnabled) DesignSystem.colors.primary else DesignSystem.colors.basePrimaryWhite,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

