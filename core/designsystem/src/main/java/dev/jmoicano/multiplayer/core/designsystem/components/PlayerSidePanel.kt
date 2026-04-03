package dev.jmoicano.multiplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem

/**
 * Side panel component used on expanded (tablet / landscape) player layouts.
 *
 * Wraps its [content] inside a rounded card with an icon header row.
 *
 * @param headerIcon Icon displayed in the header row
 * @param modifier Modifier for the composable
 * @param cornerRadius Corner radius of the side panel
 * @param backgroundColor Background color of the side panel
 * @param content LazyList content displayed below the header
 */
@Composable
fun PlayerSidePanel(
    headerIcon: Painter,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = DesignSystem.sizing.cornerRadiusExtraLarge,
    backgroundColor: Color = DesignSystem.colors.alphaInvert15,
    content: LazyListScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(vertical = DesignSystem.sizing.spacingMedium),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSystem.sizing.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                painter = headerIcon,
                contentDescription = null,
                tint = DesignSystem.colors.textPrimary,
                modifier = Modifier.size(DesignSystem.sizing.iconSizeSmall),
            )
        }

        Spacer(modifier = Modifier.height(DesignSystem.sizing.spacingSmall))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.sizing.spacingExtraSmall),
            content = content,
        )
    }
}
