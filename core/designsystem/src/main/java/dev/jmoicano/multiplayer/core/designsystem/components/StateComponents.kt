package dev.jmoicano.multiplayer.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem

/**
 * Full-screen loading indicator shown while the initial data is being fetched.
 */
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSystem.sizing.spacingMedium),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = DesignSystem.colors.primary)
    }
}

/**
 * Compact loading indicator for use inside a [androidx.compose.foundation.lazy.LazyColumn] item
 * when paginating results.
 */
@Composable
fun PaginationLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSystem.sizing.spacingMedium),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = DesignSystem.colors.primary,
        )
    }
}

/**
 * Error state shown when loading fails. Includes a retry button.
 *
 * @param message Human-readable error message
 * @param onRetry Called when the user taps the retry button
 */
@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSystem.sizing.spacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = DesignSystem.colors.error,
            style = DesignSystem.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(DesignSystem.sizing.spacingSmall))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.colors.primary),
        ) {
            Text(text = "Retry", color = DesignSystem.colors.onPrimary)
        }
    }
}

/**
 * Empty state shown when no search results are found.
 *
 * @param message Message describing the empty state
 */
@Composable
fun EmptyState(
    message: String = "No songs found.\nTry searching for your favourite artist or song.",
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSystem.sizing.spacingLarge),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = DesignSystem.colors.textSecondary,
            style = DesignSystem.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
