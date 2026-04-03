package dev.jmoicano.multiplayer.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

data class Sizing(
    val spacingExtraSmall: Dp,
    val spacingSmall: Dp,
    val spacingMedium: Dp,
    val spacingLarge: Dp,
    val spacingExtraLarge: Dp,
    val spacingHuge: Dp,
    val iconSizeExtraSmall: Dp,
    val iconSizeSmall: Dp,
    val iconSizeMedium: Dp,
    val iconSizeLarge: Dp,
    val iconSizeExtraLarge: Dp,
    val trackItemImageSize: Dp,
    val albumImageSize: Dp,
    val splashImageSize: Dp,
    val cornerRadiusExtraSmall: Dp,
    val cornerRadiusSmall: Dp,
    val cornerRadiusMedium: Dp,
    val cornerRadiusLarge: Dp,
    val cornerRadiusExtraLarge: Dp,
    /** Corner radius used for album artwork images in the player. */
    val artworkCornerRadius: Dp,
    val blurRadiusLarge: Dp,
    val dragHandleWidth: Dp,
    val dragHandleHeight: Dp,
    val sliderTrackHeight: Dp,
)

data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val error: Color,
    val onError: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val text03: Color,
    val alphaInvert10: Color,
    val alphaInvert15: Color,
    val alphaInvert25: Color,
    val backgroundAlpha01: Color,
    val backgroundGradientStart: Color,
    val backgroundGradientEnd: Color,
    val basePrimaryWhite: Color,
    /** Active (filled) segment of the player progress slider. */
    val playerSliderActive: Color,
    /** Background tint for circular player control buttons. */
    val playerButtonBackground: Color,
    /** Semi-transparent dark overlay used as a background scrim on Wear screens. */
    val wearOverlay: Color,
    /** Secondary text color for Wear OS screens (e.g. artist name). */
    val wearTextSecondary: Color,
    /** Inactive (unfilled) track color of the circular progress indicator on Wear OS. */
    val wearProgressTrack: Color,
)

data class AppTypography(
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
)

data class AppIcons(
    val cd: Int,
    val play: Int,
    val search: Int,
    val musicalNote: Int,
    val playing: Int,
    val setlist: Int,
    val searchButton: Int,
    val splash: Int,
    val musicList: Int,
    val forwardBar: Int,
    val playOnRepeat: Int,
    val pause: Int,
)

val LocalSizing = staticCompositionLocalOf<Sizing> {
    error("No Sizing provided")
}

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}

val LocalAppTypography = staticCompositionLocalOf<AppTypography> {
    error("No AppTypography provided")
}

val LocalAppIcons = staticCompositionLocalOf<AppIcons> {
    error("No AppIcons provided")
}

object DesignSystem {
    val sizing: Sizing
        @Composable
        @ReadOnlyComposable
        get() = LocalSizing.current

    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current

    val icons: AppIcons
        @Composable
        @ReadOnlyComposable
        get() = LocalAppIcons.current
}

