package dev.jmoicano.multiplayer.core.player.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.jmoicano.multiplayer.core.player.playback.DefaultExoPlayerFactory
import dev.jmoicano.multiplayer.core.player.playback.DefaultMediaPlayerFactory
import dev.jmoicano.multiplayer.core.player.playback.ExoPlayerFactory
import dev.jmoicano.multiplayer.core.player.playback.MediaPlayerFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindExoPlayerFactory(
        factory: DefaultExoPlayerFactory,
    ): ExoPlayerFactory

    @Binds
    @Singleton
    abstract fun bindMediaPlayerFactory(
        factory: DefaultMediaPlayerFactory,
    ): MediaPlayerFactory
}

