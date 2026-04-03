package dev.jmoicano.multiplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem

/**
 * Playback controls composable used on the Player screen.
 *
 * Displays a seek-bar with current position / remaining time labels, plus
 * play/pause, skip-previous, skip-next and loop buttons.
 *
 * @param progress Current playback progress in the range [0f, 1f]
 * @param onProgressChange Called while the user drags the slider
 * @param isPlaying Whether playback is currently active
 * @param onTogglePlayPause Called when play/pause is tapped
 * @param onSkipNext Called when skip-next is tapped
 * @param onSkipPrevious Called when skip-previous is tapped
 * @param onRepeatClick Called when the repeat/loop button is tapped
 * @param currentPositionText Formatted current position (e.g. "1:23")
 * @param remainingTimeText Formatted remaining time (e.g. "-2:47")
 * @param playIcon Painter for the play icon
 * @param pauseIcon Painter for the pause icon
 * @param nextIcon Painter for the next icon (mirrored 180° for previous)
 * @param repeatIcon Painter for the repeat/loop icon
 * @param modifier Modifier for the composable
 * @param isLoopEnabled Whether loop mode is currently active
 * @param activeTrackColor Colour of the filled portion of the slider track
 * @param inactiveTrackColor Colour of the unfilled portion of the slider track
 * @param thumbColor Colour of the slider thumb
 * @param textColor Colour of the position / remaining time text
 * @param iconTint Tint applied to the control icons
 * @param buttonBackgroundColor Background colour of the play/pause button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackControls(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onRepeatClick: () -> Unit,
    currentPositionText: String,
    remainingTimeText: String,
    playIcon: Painter,
    pauseIcon: Painter,
    nextIcon: Painter,
    repeatIcon: Painter,
    modifier: Modifier = Modifier,
    isLoopEnabled: Boolean = false,
    activeTrackColor: Color = DesignSystem.colors.primary,
    inactiveTrackColor: Color = DesignSystem.colors.alphaInvert25,
    thumbColor: Color = DesignSystem.colors.textPrimary,
    textColor: Color = DesignSystem.colors.textSecondary,
    iconTint: Color = DesignSystem.colors.textPrimary,
    buttonBackgroundColor: Color = DesignSystem.colors.alphaInvert15,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Seek slider
        Slider(
            value = progress,
            onValueChange = onProgressChange,
            modifier = Modifier.fillMaxWidth(),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(DesignSystem.sizing.spacingMedium)
                        .background(thumbColor, CircleShape),
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(DesignSystem.sizing.sliderTrackHeight),
                    thumbTrackGapSize = 0.dp,
                    drawStopIndicator = null,
                    colors = SliderDefaults.colors(
                        activeTrackColor = activeTrackColor,
                        inactiveTrackColor = inactiveTrackColor,
                    ),
                )
            },
        )

        // Position / remaining time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = currentPositionText, color = textColor, style = DesignSystem.typography.bodySmall)
            Text(text = remainingTimeText, color = textColor, style = DesignSystem.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(DesignSystem.sizing.spacingExtraLarge))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Play / Pause
            Box(
                modifier = Modifier
                    .size(DesignSystem.sizing.iconSizeExtraLarge)
                    .clip(CircleShape)
                    .background(buttonBackgroundColor)
                    .clickable { onTogglePlayPause() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = if (isPlaying) pauseIcon else playIcon,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = iconTint,
                    modifier = Modifier.size(DesignSystem.sizing.iconSizeMedium),
                )
            }

            Spacer(modifier = Modifier.width(DesignSystem.sizing.spacingLarge))

            // Skip previous (mirrored next icon)
            IconButton(
                onClick = onSkipPrevious,
                modifier = Modifier.size(DesignSystem.sizing.iconSizeMedium),
            ) {
                Icon(
                    painter = nextIcon,
                    contentDescription = "Previous",
                    tint = iconTint,
                    modifier = Modifier
                        .size(DesignSystem.sizing.iconSizeMedium)
                        .rotate(180f),
                )
            }

            Spacer(modifier = Modifier.width(DesignSystem.sizing.spacingLarge))

            // Skip next
            IconButton(
                onClick = onSkipNext,
                modifier = Modifier.size(DesignSystem.sizing.iconSizeMedium),
            ) {
                Icon(
                    painter = nextIcon,
                    contentDescription = "Next",
                    tint = iconTint,
                    modifier = Modifier.size(DesignSystem.sizing.iconSizeMedium),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Loop / Repeat
            IconButton(
                onClick = onRepeatClick,
                modifier = Modifier.size(DesignSystem.sizing.iconSizeSmall),
            ) {
                Icon(
                    painter = repeatIcon,
                    contentDescription = "Repeat",
                    tint = if (isLoopEnabled) DesignSystem.colors.primary else iconTint,
                    modifier = Modifier.size(DesignSystem.sizing.iconSizeSmall),
                )
            }
        }
    }
}

