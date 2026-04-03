package dev.jmoicano.multiplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem

/**
 * Header component displaying album artwork together with a title and subtitle.
 *
 * Supports both portrait (vertical stack) and landscape / tablet (horizontal row) layouts
 * via the [isTablet] flag.
 *
 * @param title Primary text (track name / album name)
 * @param subtitle Secondary text (artist name)
 * @param artworkUrl URL of the cover art (100×100 thumbnail; automatically upscaled to 600×600)
 * @param modifier Modifier for the composable
 * @param showImage Whether to show the artwork image
 * @param textAlign Alignment for the text when in portrait layout
 * @param titleStyle [TextStyle] used for the title
 * @param subtitleStyle [TextStyle] used for the subtitle
 * @param fontWeight [FontWeight] for the title
 * @param textColor Colour of the title
 * @param subtitleColor Colour of the subtitle
 * @param imageSize Size of the artwork image
 * @param fillSpace Whether the artwork should fill the available vertical space
 * @param isTablet When true a horizontal row layout is used
 */
@Composable
fun AlbumHeader(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    modifier: Modifier = Modifier,
    showImage: Boolean = true,
    textAlign: TextAlign = TextAlign.Center,
    titleStyle: TextStyle = DesignSystem.typography.headlineMedium,
    subtitleStyle: TextStyle = DesignSystem.typography.titleMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    textColor: Color = DesignSystem.colors.textPrimary,
    subtitleColor: Color = DesignSystem.colors.textSecondary,
    imageSize: Dp = DesignSystem.sizing.albumImageSize,
    fillSpace: Boolean = false,
    isTablet: Boolean = false,
) {
    if (isTablet) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showImage) {
                AsyncImage(
                    model = artworkUrl?.replace("100x100", "600x600"),
                    contentDescription = null,
                    modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(DesignSystem.sizing.cornerRadiusMedium))
                        .background(DesignSystem.colors.alphaInvert10),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(DesignSystem.sizing.spacingLarge))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = textColor,
                    style = titleStyle,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = subtitle,
                    color = subtitleColor,
                    style = subtitleStyle,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            if (showImage) {
                Box(
                    modifier = if (fillSpace) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = artworkUrl?.replace("100x100", "600x600"),
                        contentDescription = null,
                        modifier = Modifier
                            .size(imageSize)
                            .clip(RoundedCornerShape(DesignSystem.sizing.cornerRadiusMedium))
                            .background(DesignSystem.colors.alphaInvert10),
                        contentScale = ContentScale.Crop,
                    )
                }
                Spacer(modifier = Modifier.height(DesignSystem.sizing.spacingMedium))
            }
            Text(
                text = title,
                color = textColor,
                style = titleStyle,
                fontWeight = fontWeight,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = subtitle,
                color = subtitleColor,
                style = subtitleStyle,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

