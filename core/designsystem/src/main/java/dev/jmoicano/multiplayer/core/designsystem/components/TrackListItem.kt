package dev.jmoicano.multiplayer.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import dev.jmoicano.multiplayer.core.designsystem.R
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem

/**
 * List item component displaying a single track in the search results.
 *
 * Uses [DesignSystem] tokens for all visual properties, ensuring consistent
 * theming across the entire application.
 *
 * @param trackName Name of the track
 * @param artistName Name of the artist
 * @param artworkUrl URL of the track artwork (100x100)
 * @param onClick Callback when the item is clicked
 * @param modifier Modifier for the list item
 * @param onMoreClick Optional callback when the more-options button is clicked
 * @param trailingContent Optional custom trailing content (replaces the more-options button)
 */
@Composable
fun TrackListItem(
    trackName: String,
    artistName: String,
    artworkUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMoreClick: (() -> Unit)? = null,
    trailingContent: @Composable (BoxScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = DesignSystem.sizing.spacingMedium,
                vertical = DesignSystem.sizing.spacingSmall,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = artworkUrl,
            contentDescription = null,
            modifier = Modifier
                .size(DesignSystem.sizing.trackItemImageSize)
                .clip(RoundedCornerShape(DesignSystem.sizing.cornerRadiusSmall)),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(DesignSystem.sizing.spacingMedium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trackName,
                style = DesignSystem.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = DesignSystem.colors.textPrimary,
                maxLines = 1,
            )
            Text(
                text = artistName,
                style = DesignSystem.typography.bodyMedium,
                color = DesignSystem.colors.textSecondary,
                maxLines = 1,
            )
        }

        Box(contentAlignment = Alignment.Center) {
            when {
                trailingContent != null -> trailingContent()
                onMoreClick != null -> IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.common_more_options),
                        tint = DesignSystem.colors.textSecondary,
                    )
                }
            }
        }
    }
}
