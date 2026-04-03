package dev.jmoicano.multiplayer.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem

/**
 * Standard top app bar used across all screens.
 *
 * Uses [DesignSystem] tokens so that the bar automatically adapts to the
 * current theme (dark / light).
 *
 * @param title Title text displayed in the app bar
 * @param modifier Modifier for the app bar
 * @param navigationIcon Optional leading navigation icon composable
 * @param actions Optional trailing action icons composable
 * @param containerColor Background colour of the bar (defaults to [DesignSystem.colors.background])
 * @param titleContentColor Colour of the title text
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = DesignSystem.colors.background,
    titleContentColor: Color = DesignSystem.colors.textPrimary,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = DesignSystem.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = titleContentColor,
                maxLines = 1,
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = titleContentColor,
            navigationIconContentColor = titleContentColor,
            actionIconContentColor = titleContentColor,
        ),
        modifier = modifier,
    )
}

