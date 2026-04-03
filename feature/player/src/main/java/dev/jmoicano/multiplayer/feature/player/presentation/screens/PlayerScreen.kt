package dev.jmoicano.multiplayer.feature.player.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import dev.jmoicano.multiplayer.core.designsystem.components.ActionSheetMenuItem
import dev.jmoicano.multiplayer.core.designsystem.components.ActionSheetTrackInfo
import dev.jmoicano.multiplayer.core.designsystem.components.ErrorMessage
import dev.jmoicano.multiplayer.core.designsystem.components.LoadingIndicator
import dev.jmoicano.multiplayer.core.designsystem.components.PlaybackControls
import dev.jmoicano.multiplayer.core.designsystem.components.PlayerSidePanel
import dev.jmoicano.multiplayer.core.designsystem.components.StandardTopAppBar
import dev.jmoicano.multiplayer.core.designsystem.components.TrackListItem
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem
import dev.jmoicano.multiplayer.feature.player.R
import dev.jmoicano.multiplayer.feature.player.presentation.PlayerUiState
import dev.jmoicano.multiplayer.feature.player.presentation.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    trackId: Long,
    viewModel: PlayerViewModel = hiltViewModel(),
    isTabletLayout: Boolean = false,
    onBack: () -> Unit = {},
    onViewAlbumClick: (Long) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var isOptionsSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(trackId) {
        viewModel.load(trackId)
    }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(R.string.player_title_now_playing),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.common_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isOptionsSheetVisible = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.player_options),
                        )
                    }
                },
            )
        },
        containerColor = DesignSystem.colors.background,
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            state.error != null -> {
                ErrorMessage(
                    message = state.error ?: stringResource(R.string.player_error),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            else -> {
                if (isTabletLayout) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DesignSystem.colors.background)
                            .padding(paddingValues)
                            .padding(horizontal = DesignSystem.sizing.spacingLarge),
                    ) {
                        PlayerMainContent(
                            state = state,
                            artworkSize = 286.dp,
                            onProgressChange = viewModel::seekToProgress,
                            onTogglePlayPause = viewModel::togglePlayPause,
                            onSkipNext = viewModel::playNext,
                            onSkipPrevious = viewModel::playPrevious,
                            onRepeatClick = viewModel::toggleLoop,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = DesignSystem.sizing.spacingLarge),
                        )

                        PlayerSidePanel(
                            headerIcon = painterResource(DesignSystem.icons.musicList),
                            cornerRadius = DesignSystem.sizing.cornerRadiusExtraLarge,
                            backgroundColor = DesignSystem.colors.alphaInvert15,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(288.dp),
                        ) {
                            items(
                                count = state.playlist.size,
                                key = { index -> state.playlist[index].trackId },
                            ) { index ->
                                val track = state.playlist[index]
                                TrackListItem(
                                    trackName = track.trackName,
                                    artistName = track.artistName,
                                    artworkUrl = track.artworkUrl100,
                                    onClick = { viewModel.playTrackAt(index) },
                                    trailingContent = {
                                        if (index == state.currentIndex) {
                                            Icon(
                                                painter = painterResource(DesignSystem.icons.playing),
                                                contentDescription = stringResource(R.string.player_title_now_playing),
                                                tint = DesignSystem.colors.textPrimary,
                                                modifier = Modifier.size(DesignSystem.sizing.iconSizeSmall),
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                } else {
                    PlayerMainContent(
                        state = state,
                        artworkSize = 264.dp,
                        onProgressChange = viewModel::seekToProgress,
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onSkipNext = viewModel::playNext,
                        onSkipPrevious = viewModel::playPrevious,
                        onRepeatClick = viewModel::toggleLoop,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DesignSystem.colors.background)
                            .padding(paddingValues)
                            .padding(horizontal = DesignSystem.sizing.spacingLarge),
                    )
                }
            }
        }

        if (isOptionsSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isOptionsSheetVisible = false },
                containerColor = DesignSystem.colors.surface,
            ) {
                val currentTrack = state.currentTrack
                ActionSheetTrackInfo(
                    trackName = currentTrack?.trackName.orEmpty(),
                    artistName = currentTrack?.artistName.orEmpty(),
                )
                ActionSheetMenuItem(
                    text = stringResource(R.string.player_view_album),
                    icon = painterResource(DesignSystem.icons.setlist),
                    onClick = {
                        currentTrack?.trackId?.let(onViewAlbumClick)
                        isOptionsSheetVisible = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PlayerMainContent(
    state: PlayerUiState,
    artworkSize: androidx.compose.ui.unit.Dp,
    onProgressChange: (Float) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onRepeatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentTrack = state.currentTrack
    val durationMs = state.durationMs.coerceAtLeast(0L)
    val positionMs = state.positionMs.coerceIn(0L, durationMs.takeIf { it > 0L } ?: 0L)
    val remainingMs = (durationMs - positionMs).coerceAtLeast(0L)
    val progress = if (durationMs > 0L) {
        positionMs.toFloat() / durationMs.toFloat()
    } else {
        0f
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = currentTrack?.artworkUrl100,
                contentDescription = currentTrack?.collectionName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(artworkSize)
                    .clip(RoundedCornerShape(DesignSystem.sizing.artworkCornerRadius)),
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = currentTrack?.trackName.orEmpty(),
                color = DesignSystem.colors.textPrimary,
                style = DesignSystem.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = currentTrack?.artistName.orEmpty(),
                    color = DesignSystem.colors.textSecondary,
                    style = DesignSystem.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignSystem.sizing.spacingSmall))

        PlaybackControls(
            progress = progress,
            onProgressChange = onProgressChange,
            isPlaying = state.isPlaying,
            onTogglePlayPause = onTogglePlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onRepeatClick = onRepeatClick,
            currentPositionText = formatTime(positionMs),
            remainingTimeText = "-${formatTime(remainingMs)}",
            playIcon = painterResource(DesignSystem.icons.play),
            pauseIcon = painterResource(DesignSystem.icons.pause),
            nextIcon = painterResource(DesignSystem.icons.forwardBar),
            repeatIcon = painterResource(DesignSystem.icons.playOnRepeat),
            isLoopEnabled = state.isLoopEnabled,
            activeTrackColor = DesignSystem.colors.playerSliderActive,
            inactiveTrackColor = DesignSystem.colors.alphaInvert25,
            thumbColor = DesignSystem.colors.basePrimaryWhite,
            textColor = DesignSystem.colors.textSecondary,
            iconTint = DesignSystem.colors.textPrimary,
            buttonBackgroundColor = DesignSystem.colors.playerButtonBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DesignSystem.sizing.spacingLarge),
        )
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
