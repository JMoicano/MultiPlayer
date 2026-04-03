package dev.jmoicano.multiplayer.app.phone.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import dev.jmoicano.multiplayer.core.designsystem.R
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem
import dev.jmoicano.multiplayer.core.designsystem.theme.MPGradients

/**
 * Splash screen displayed when the app starts.
 *
 * Shows the app logo centered on a gradient background.
 * The splash screen is automatically shown for 2 seconds before transitioning
 * to the main content via [dev.jmoicano.multiplayer.app.phone.ui.navigation.AppNavigation].
 *
 * @param modifier Modifier for customizing the splash screen layout
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MPGradients.Splash),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_splash_logo),
            contentDescription = null,
            modifier = Modifier
                .size(DesignSystem.sizing.splashImageSize)
                .scale(scale.value)
        )
    }
}