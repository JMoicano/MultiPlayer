package dev.jmoicano.multiplayer.appwear.presentation.theme

import androidx.compose.runtime.Composable
import dev.jmoicano.multiplayer.core.designsystem.theme.MultiPlayerTheme as CoreMultiPlayerTheme

@Composable
fun MultiPlayerTheme(
    content: @Composable () -> Unit,
) {
    CoreMultiPlayerTheme(content = content)
}