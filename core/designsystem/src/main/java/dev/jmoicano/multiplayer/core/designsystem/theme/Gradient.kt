package dev.jmoicano.multiplayer.core.designsystem.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object MPGradients {

    val Splash = Brush.linearGradient(
        colors = listOf(
            Color(0xFF000000),
            Color(0xFF0086A0)
        ),
        start = Offset(0f, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, 0f)
    )
}