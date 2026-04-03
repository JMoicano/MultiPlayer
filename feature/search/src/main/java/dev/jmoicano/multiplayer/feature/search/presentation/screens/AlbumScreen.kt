package dev.jmoicano.multiplayer.feature.search.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.jmoicano.multiplayer.core.designsystem.components.AlbumHeader
import dev.jmoicano.multiplayer.core.designsystem.components.EmptyState
import dev.jmoicano.multiplayer.core.designsystem.components.ErrorMessage
import dev.jmoicano.multiplayer.core.designsystem.components.LoadingIndicator
import dev.jmoicano.multiplayer.core.designsystem.components.StandardTopAppBar
import dev.jmoicano.multiplayer.core.designsystem.components.TrackListItem
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem
import dev.jmoicano.multiplayer.feature.search.R
import dev.jmoicano.multiplayer.feature.search.presentation.TrackDetailsViewModel

@Composable
fun AlbumScreen(
    trackId: Long,
    viewModel: TrackDetailsViewModel = hiltViewModel(),
    isTabletLayout: Boolean = false,
    onBack: () -> Unit = {},
    onTrackClick: (Long) -> Unit = {},
) {
    val track by viewModel.track.collectAsState()
    val albumTracks by viewModel.albumTracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(trackId) {
        viewModel.loadTrackDetails(trackId)
    }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = track?.collectionName ?: stringResource(R.string.screen_title_album),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.common_back),
                            tint = DesignSystem.colors.textPrimary,
                        )
                    }
                },
            )
        },
        containerColor = DesignSystem.colors.background,
    ) { paddingValues ->
        when {
            isLoading -> LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            error != null -> ErrorMessage(
                message = error ?: stringResource(R.string.error_loading_album),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            track != null -> {
                val data = track!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DesignSystem.colors.background)
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = DesignSystem.sizing.spacingLarge),
                ) {
                    AlbumHeader(
                        title = data.collectionName ?: data.trackName,
                        subtitle = data.artistName,
                        artworkUrl = data.artworkUrl100,
                        isTablet = isTabletLayout,
                    )

                    if (albumTracks.isEmpty()) {
                        EmptyState(
                            message = stringResource(R.string.empty_album_tracks),
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = DesignSystem.sizing.spacingLarge),
                        ) {
                            itemsIndexed(
                                items = albumTracks,
                                key = { index, albumTrack -> "${albumTrack.trackId}_$index" },
                            ) { _, albumTrack ->
                                TrackListItem(
                                    trackName = albumTrack.trackName,
                                    artistName = albumTrack.artistName,
                                    artworkUrl = albumTrack.artworkUrl100,
                                    onClick = { onTrackClick(albumTrack.trackId) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
