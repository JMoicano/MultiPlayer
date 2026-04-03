package dev.jmoicano.multiplayer.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Room entity used to persist searched or recently played tracks. */
@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["collectionId"]),
        Index(value = ["trackName"]),
        Index(value = ["artistName"]),
    ],
)
data class TrackEntity(
    val wrapperType: String? = null,
    val kind: String? = null,
    val collectionId: Long? = null,
    @PrimaryKey
    val trackId: Long,
    val artistName: String,
    val collectionName: String? = null,
    val trackName: String,
    val collectionCensoredName: String? = null,
    val trackCensoredName: String? = null,
    val artistViewUrl: String? = null,
    val collectionViewUrl: String? = null,
    val trackViewUrl: String? = null,
    val previewUrl: String? = null,
    val artworkUrl30: String? = null,
    val artworkUrl60: String? = null,
    val artworkUrl100: String? = null,
    val collectionPrice: Double? = null,
    val trackPrice: Double? = null,
    val releaseDate: String? = null,
    val collectionExplicitness: String? = null,
    val trackExplicitness: String? = null,
    val discNumber: Int? = null,
    val trackNumber: Int? = null,
    val trackTimeMillis: Long? = null,
    val country: String? = null,
    val currency: String? = null,
    val primaryGenreName: String? = null,
    val isStreamable: Boolean? = null,
    val hasLyrics: Boolean? = null,
)

