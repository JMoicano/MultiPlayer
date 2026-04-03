package dev.jmoicano.multiplayer.core.player.playback

import android.content.Context
import android.media.MediaPlayer
import androidx.media3.exoplayer.ExoPlayer
import javax.inject.Inject

/** Abstract factory for [ExoPlayer] instances. */
interface ExoPlayerFactory {
    /** Creates a new [ExoPlayer] configured for [context]. */
    fun create(context: Context): ExoPlayer
}

/** Default implementation that creates [ExoPlayer] on demand. */
class DefaultExoPlayerFactory @Inject constructor() : ExoPlayerFactory {
    override fun create(context: Context): ExoPlayer = ExoPlayer.Builder(context).build()
}

/** Abstract factory for [MediaPlayer] instances. */
interface MediaPlayerFactory {
    /** Creates a new [MediaPlayer]. */
    fun create(): MediaPlayer
}

/** Default [MediaPlayerFactory] implementation. */
class DefaultMediaPlayerFactory @Inject constructor() : MediaPlayerFactory {
    override fun create(): MediaPlayer = MediaPlayer()
}

