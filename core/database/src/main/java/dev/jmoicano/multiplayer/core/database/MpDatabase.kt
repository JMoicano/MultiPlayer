package dev.jmoicano.multiplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.jmoicano.multiplayer.core.database.dao.TrackDao
import dev.jmoicano.multiplayer.core.database.model.TrackEntity

@Database(
    entities = [TrackEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MpDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object {
        const val NAME = "multiplayer.db"
    }
}

