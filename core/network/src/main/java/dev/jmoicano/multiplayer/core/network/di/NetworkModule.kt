package dev.jmoicano.multiplayer.core.network.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.jmoicano.multiplayer.core.network.client.HttpClientFactory
import dev.jmoicano.multiplayer.core.network.datasource.RemoteDataSource
import dev.jmoicano.multiplayer.core.network.datasource.iTunesRemoteDataSource
import dev.jmoicano.multiplayer.core.network.repository.DefaultSearchRepository
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository
import io.ktor.client.HttpClient
import javax.inject.Singleton

/** Registers network dependencies and the default remote repository in the Hilt graph. */
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    /** Binds the iTunes implementation to the remote data source contract. */
    @Binds
    @Singleton
    abstract fun bindRemoteDataSource(
        remoteDataSource: iTunesRemoteDataSource,
    ): RemoteDataSource

    /** Exposes the pure remote repository for compositions that do not use local cache. */
    @Binds
    @Singleton
    @RemoteSearchRepository
    abstract fun bindRemoteSearchRepository(
        repository: DefaultSearchRepository,
    ): SearchRepository

    companion object {
        /** Provides the shared HTTP client for the network layer. */
        @Provides
        @Singleton
        fun provideHttpClient(): HttpClient = HttpClientFactory.createHttpClient()
    }
}

