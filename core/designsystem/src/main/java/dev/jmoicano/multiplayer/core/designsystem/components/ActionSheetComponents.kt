package dev.jmoicano.multiplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem

/**
 * Displays track name and artist name in the action sheet header.
 *
 * @param trackName Name of the track
 * @param artistName Name of the artist
 * @param modifier Modifier for the composable
 */
@Composable
fun ActionSheetTrackInfo(
    trackName: String,
    artistName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.sizing.spacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = trackName,
            color = DesignSystem.colors.textPrimary,
            style = DesignSystem.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = artistName,
            color = DesignSystem.colors.textSecondary,
            style = DesignSystem.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * A single row menu item used inside a bottom-sheet action menu.
 *
 * @param text Label for the menu item
 * @param icon Leading icon painter
 * @param onClick Called when the item is tapped
 * @param modifier Modifier for the composable
 * @param trailingIcon Optional trailing icon painter
 */
@Composable
fun ActionSheetMenuItem(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: Painter? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = DesignSystem.sizing.spacingLarge,
                vertical = DesignSystem.sizing.spacingMedium,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = DesignSystem.colors.textPrimary,
            modifier = Modifier.size(DesignSystem.sizing.iconSizeSmall),
        )
        Spacer(modifier = Modifier.width(DesignSystem.sizing.spacingMedium))
        Text(
            text = text,
            color = DesignSystem.colors.textPrimary,
            style = DesignSystem.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (trailingIcon != null) {
            Icon(
                painter = trailingIcon,
                contentDescription = null,
                tint = DesignSystem.colors.textSecondary,
                modifier = Modifier.size(DesignSystem.sizing.iconSizeSmall),
            )
        }
    }
}

