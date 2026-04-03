package dev.jmoicano.multiplayer.feature.search.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import dev.jmoicano.multiplayer.core.designsystem.components.ActionSheetMenuItem
import dev.jmoicano.multiplayer.core.designsystem.components.ActionSheetTrackInfo
import dev.jmoicano.multiplayer.core.designsystem.components.EmptyState
import dev.jmoicano.multiplayer.core.designsystem.components.ErrorMessage
import dev.jmoicano.multiplayer.core.designsystem.components.LoadingIndicator
import dev.jmoicano.multiplayer.core.designsystem.components.SearchTextField
import dev.jmoicano.multiplayer.core.designsystem.components.TrackListItem
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.feature.search.presentation.SearchViewModel

/**
 * Main track search and browsing screen with Paging 3 integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsListScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    isTabletLayout: Boolean = false,
    onTrackClick: (Long) -> Unit = {},
    onViewAlbumClick: (Long) -> Unit = {},
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val hasActiveQuery = searchQuery.isNotBlank()
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedTrackForMenu by remember { mutableStateOf<Track?>(null) }
    var expandedTrackMenuId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.screen_title_songs),
                        style = DesignSystem.typography.titleLarge,
                        color = DesignSystem.colors.textPrimary,
                    )
                },
                actions = {
                    if (!isSearchExpanded) {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.search_content_description),
                                tint = DesignSystem.colors.textPrimary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DesignSystem.colors.background,
                ),
            )
        },
        containerColor = DesignSystem.colors.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DesignSystem.colors.background)
                .padding(paddingValues),
        ) {
            if (isSearchExpanded) {
                SearchTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.search_placeholder),
                    leadingIcon = painterResource(DesignSystem.icons.search),
                    modifier = Modifier.padding(horizontal = DesignSystem.sizing.spacingMedium),
                )
            }

            val pagingItems = viewModel.tracks.collectAsLazyPagingItems()

            LaunchedEffect(searchQuery, pagingItems.itemSnapshotList) {
                val snapshotTracks = pagingItems.itemSnapshotList.items.filterNotNull()
                viewModel.syncBrowseSnapshot(snapshotTracks)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DesignSystem.colors.background),
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = { index ->
                        val track = pagingItems[index]
                        if (track != null) "${track.trackId}_$index" else index
                    }
                ) { index ->
                    val track = pagingItems[index]
                    if (track != null) {
                        TrackListItem(
                            trackName = track.trackName,
                            artistName = track.artistName,
                            artworkUrl = track.artworkUrl100,
                            onClick = { onTrackClick(track.trackId) },
                            onMoreClick = if (isTabletLayout) null else {
                                { selectedTrackForMenu = track }
                            },
                            trailingContent = if (isTabletLayout) {
                                {
                                    IconButton(onClick = { expandedTrackMenuId = track.trackId }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = stringResource(dev.jmoicano.multiplayer.core.designsystem.R.string.common_more_options),
                                            tint = DesignSystem.colors.textSecondary,
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expandedTrackMenuId == track.trackId,
                                        onDismissRequest = { expandedTrackMenuId = null },
                                        containerColor = DesignSystem.colors.surface,
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(text = stringResource(dev.jmoicano.multiplayer.feature.search.R.string.action_view_album)) },
                                            onClick = {
                                                expandedTrackMenuId = null
                                                onViewAlbumClick(track.trackId)
                                            },
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                        )
                    }
                }

                when (pagingItems.loadState.append) {
                    is LoadState.Loading -> {
                        if (hasActiveQuery) {
                            item {
                                LoadingIndicator()
                            }
                        }
                    }

                    is LoadState.Error -> {
                        if (hasActiveQuery) {
                            item {
                                ErrorMessage(
                                    message = "Failed to load more songs",
                                    onRetry = { pagingItems.retry() }
                                )
                            }
                        }
                    }

                    else -> {}
                }

                when (pagingItems.loadState.refresh) {
                    is LoadState.Loading -> {
                        if (hasActiveQuery) {
                            item {
                                LoadingIndicator()
                            }
                        }
                    }

                    is LoadState.Error -> {
                        if (hasActiveQuery) {
                            item {
                                ErrorMessage(
                                    message = "Failed to load songs",
                                    onRetry = { pagingItems.refresh() }
                                )
                            }
                        }
                    }

                    else -> {
                        if (pagingItems.itemCount == 0 && hasActiveQuery) {
                            item {
                                EmptyState()
                            }
                        }
                    }
                }
            }
        }

        if (!isTabletLayout && selectedTrackForMenu != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedTrackForMenu = null },
                containerColor = DesignSystem.colors.surface,
            ) {
                val track = selectedTrackForMenu ?: return@ModalBottomSheet
                ActionSheetTrackInfo(
                    trackName = track.trackName,
                    artistName = track.artistName,
                )
                ActionSheetMenuItem(
                    text = stringResource(dev.jmoicano.multiplayer.feature.search.R.string.action_view_album),
                    icon = painterResource(DesignSystem.icons.setlist),
                    onClick = {
                        onViewAlbumClick(track.trackId)
                        selectedTrackForMenu = null
                    },
                )
            }
        }
    }
}