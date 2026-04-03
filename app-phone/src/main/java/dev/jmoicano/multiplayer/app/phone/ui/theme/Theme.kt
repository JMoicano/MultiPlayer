package dev.jmoicano.multiplayer.app.phone.ui.theme

import androidx.compose.runtime.Composable
import dev.jmoicano.multiplayer.core.designsystem.theme.MultiPlayerTheme

/**
 * App-phone theme wrapper. Delegates to [MultiPlayerTheme] which provides
 * all [dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem] composition locals.
 */
@Composable
fun AppTheme(
    content: @Composable () -> Unit,
) {
    MultiPlayerTheme(content = content)
}