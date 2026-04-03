package dev.jmoicano.multiplayer.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.jmoicano.multiplayer.core.database.MpDatabase
import dev.jmoicano.multiplayer.core.database.dao.TrackDao
import dev.jmoicano.multiplayer.core.database.datasource.LocalTrackDataSource
import dev.jmoicano.multiplayer.core.database.datasource.RoomLocalTrackDataSource
import dev.jmoicano.multiplayer.core.database.repository.OfflineFirstSearchRepository
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository
import javax.inject.Singleton

/** Registers Room cache and the offline-first repository in the Hilt graph. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    /** Binds the Room implementation to the local data source contract. */
    @Binds
    @Singleton
    abstract fun bindLocalTrackDataSource(
        localTrackDataSource: RoomLocalTrackDataSource,
    ): LocalTrackDataSource

    /** Exposes the local-fallback repository as the default search implementation. */
    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        repository: OfflineFirstSearchRepository,
    ): SearchRepository

    companion object {
        /** Creates the app-wide singleton instance of the local database. */
        @Provides
        @Singleton
        fun provideMpDatabase(@ApplicationContext context: Context): MpDatabase =
            Room.databaseBuilder(
                context,
                MpDatabase::class.java,
                MpDatabase.NAME,
            ).fallbackToDestructiveMigration().build()

        /** Exposes the track DAO from the database instance. */
        @Provides
        fun provideTrackDao(database: MpDatabase): TrackDao = database.trackDao()
    }
}

