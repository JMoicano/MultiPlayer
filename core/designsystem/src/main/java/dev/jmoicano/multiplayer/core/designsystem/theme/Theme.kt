package dev.jmoicano.multiplayer.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.core.view.WindowCompat
import dev.jmoicano.multiplayer.core.designsystem.R

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
    onError = OnErrorDark,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

@Composable
fun MultiPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val sizing = Sizing(
        spacingExtraSmall = dimensionResource(R.dimen.spacing_extra_small),
        spacingSmall = dimensionResource(R.dimen.spacing_small),
        spacingMedium = dimensionResource(R.dimen.spacing_medium),
        spacingLarge = dimensionResource(R.dimen.spacing_large),
        spacingExtraLarge = dimensionResource(R.dimen.spacing_extra_large),
        spacingHuge = dimensionResource(R.dimen.spacing_huge),
        iconSizeExtraSmall = dimensionResource(R.dimen.icon_size_extra_small),
        iconSizeSmall = dimensionResource(R.dimen.icon_size_small),
        iconSizeMedium = dimensionResource(R.dimen.icon_size_medium),
        iconSizeLarge = dimensionResource(R.dimen.icon_size_large),
        iconSizeExtraLarge = dimensionResource(R.dimen.icon_size_extra_large),
        trackItemImageSize = dimensionResource(R.dimen.track_item_image_size),
        albumImageSize = dimensionResource(R.dimen.album_image_size),
        splashImageSize = dimensionResource(R.dimen.splash_image_size),
        cornerRadiusExtraSmall = dimensionResource(R.dimen.corner_radius_extra_small),
        cornerRadiusSmall = dimensionResource(R.dimen.corner_radius_small),
        cornerRadiusMedium = dimensionResource(R.dimen.corner_radius_medium),
        cornerRadiusLarge = dimensionResource(R.dimen.corner_radius_large),
        cornerRadiusExtraLarge = dimensionResource(R.dimen.corner_radius_extra_large),
        artworkCornerRadius = dimensionResource(R.dimen.artwork_corner_radius),
        blurRadiusLarge = dimensionResource(R.dimen.blur_radius_large),
        dragHandleWidth = dimensionResource(R.dimen.drag_handle_width),
        dragHandleHeight = dimensionResource(R.dimen.drag_handle_height),
        sliderTrackHeight = dimensionResource(R.dimen.slider_track_height),
    )

    val colors = if (darkTheme) {
        AppColors(
            primary = colorScheme.primary,
            onPrimary = colorScheme.onPrimary,
            primaryContainer = colorScheme.primaryContainer,
            onPrimaryContainer = colorScheme.onPrimaryContainer,
            secondary = colorScheme.secondary,
            onSecondary = colorScheme.onSecondary,
            secondaryContainer = colorScheme.secondaryContainer,
            onSecondaryContainer = colorScheme.onSecondaryContainer,
            tertiary = colorScheme.tertiary,
            onTertiary = colorScheme.onTertiary,
            tertiaryContainer = colorScheme.tertiaryContainer,
            onTertiaryContainer = colorScheme.onTertiaryContainer,
            background = colorScheme.background,
            onBackground = colorScheme.onBackground,
            surface = colorScheme.surface,
            onSurface = colorScheme.onSurface,
            error = colorScheme.error,
            onError = colorScheme.onError,
            textPrimary = TextPrimaryDark,
            textSecondary = TextSecondaryDark,
            text03 = Text03Dark,
            alphaInvert10 = AlphaInvert10Dark,
            alphaInvert15 = AlphaInvert15Dark,
            alphaInvert25 = AlphaInvert25Dark,
            backgroundAlpha01 = BackgroundAlpha01Dark,
            backgroundGradientStart = BackgroundGradientStartDark,
            backgroundGradientEnd = BackgroundGradientEndDark,
            basePrimaryWhite = Color.White,
            playerSliderActive = PlayerSliderActiveDark,
            playerButtonBackground = PlayerButtonBackgroundDark,
            wearOverlay = WearOverlay,
            wearTextSecondary = WearTextSecondaryDark,
            wearProgressTrack = WearProgressTrackDark,
        )
    } else {
        AppColors(
            primary = colorScheme.primary,
            onPrimary = colorScheme.onPrimary,
            primaryContainer = colorScheme.primaryContainer,
            onPrimaryContainer = colorScheme.onPrimaryContainer,
            secondary = colorScheme.secondary,
            onSecondary = colorScheme.onSecondary,
            secondaryContainer = colorScheme.secondaryContainer,
            onSecondaryContainer = colorScheme.onSecondaryContainer,
            tertiary = colorScheme.tertiary,
            onTertiary = colorScheme.onTertiary,
            tertiaryContainer = colorScheme.tertiaryContainer,
            onTertiaryContainer = colorScheme.onTertiaryContainer,
            background = colorScheme.background,
            onBackground = colorScheme.onBackground,
            surface = colorScheme.surface,
            onSurface = colorScheme.onSurface,
            error = colorScheme.error,
            onError = colorScheme.onError,
            textPrimary = TextPrimaryLight,
            textSecondary = TextSecondaryLight,
            text03 = Text03Light,
            alphaInvert10 = AlphaInvert10Light,
            alphaInvert15 = AlphaInvert15Light,
            alphaInvert25 = AlphaInvert25Light,
            backgroundAlpha01 = BackgroundAlpha01Light,
            backgroundGradientStart = BackgroundGradientStartLight,
            backgroundGradientEnd = BackgroundGradientEndLight,
            basePrimaryWhite = Color.White,
            playerSliderActive = PlayerSliderActiveLight,
            playerButtonBackground = PlayerButtonBackgroundLight,
            wearOverlay = WearOverlay,
            wearTextSecondary = WearTextSecondaryLight,
            wearProgressTrack = WearProgressTrackLight,
        )
    }

    val typography = AppTypography(
        headlineLarge = Typography.headlineLarge,
        headlineMedium = Typography.headlineMedium,
        titleLarge = Typography.titleLarge,
        titleMedium = Typography.titleMedium,
        bodyLarge = Typography.bodyLarge,
        bodyMedium = Typography.bodyMedium,
        bodySmall = Typography.bodySmall,
    )

    val icons = AppIcons(
        cd = R.drawable.ic_cd,
        play = R.drawable.ic_play,
        search = R.drawable.ic_search,
        musicalNote = R.drawable.ic_splash_logo,
        playing = R.drawable.ic_playing,
        setlist = R.drawable.ic_setlist,
        searchButton = R.drawable.ic_search_button,
        splash = R.drawable.ic_splash_logo,
        musicList = R.drawable.ic_music_list,
        forwardBar = R.drawable.ic_forward_bar,
        playOnRepeat = R.drawable.ic_play_on_repeat,
        pause = android.R.drawable.ic_media_pause,
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalSizing provides sizing,
        LocalAppColors provides colors,
        LocalAppTypography provides typography,
        LocalAppIcons provides icons,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
