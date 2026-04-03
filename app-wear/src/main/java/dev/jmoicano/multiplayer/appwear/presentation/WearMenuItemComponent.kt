package dev.jmoicano.multiplayer.appwear.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import coil.compose.AsyncImage
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem

private val ItemShape = RoundedCornerShape(8.dp)
private val IconShape = RoundedCornerShape(8.dp)

/** Screen-level title: primary text in textPrimary colour, optional secondary in wearTextSecondary. */
@Composable
fun WearScreenTitle(
    primary: String,
    secondary: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = primary,
            style = DesignSystem.typography.titleMedium.copy(
                color = DesignSystem.colors.basePrimaryWhite,
            ),
        )
        if (secondary != null) {
            Text(
                text = secondary,
                style = DesignSystem.typography.bodyMedium.copy(
                    color = DesignSystem.colors.wearTextSecondary,
                ),
            )
        }
    }
}

/** Navigation item: icon from a drawable resource with a tinted background. */
@Composable
fun WearMenuNavItem(
    label: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(ItemShape)
            .background(DesignSystem.colors.alphaInvert15)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(IconShape)
                .background(DesignSystem.colors.alphaInvert10),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            style = DesignSystem.typography.titleMedium.copy(
                color = DesignSystem.colors.basePrimaryWhite,
            ),
        )
    }
}

/** Track item: artwork loaded from URL, track name + artist name. */
@Composable
fun WearMenuTrackItem(
    trackName: String,
    artistName: String,
    artworkUrl: String?,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemBackground = if (isActive) DesignSystem.colors.alphaInvert25 else DesignSystem.colors.alphaInvert15

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(ItemShape)
            .background(itemBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(IconShape)
                .background(DesignSystem.colors.alphaInvert10),
        ) {
            AsyncImage(
                model = artworkUrl,
                contentDescription = trackName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(IconShape),
                placeholder = painterResource(id = dev.jmoicano.multiplayer.core.designsystem.R.drawable.ic_musical_note),
                error = painterResource(id = dev.jmoicano.multiplayer.core.designsystem.R.drawable.ic_musical_note),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = trackName,
                style = DesignSystem.typography.titleMedium.copy(
                    color = DesignSystem.colors.basePrimaryWhite,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = artistName,
                style = DesignSystem.typography.bodyMedium.copy(
                    color = DesignSystem.colors.wearTextSecondary,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
