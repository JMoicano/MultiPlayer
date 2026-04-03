package dev.jmoicano.multiplayer.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Paginated response returned by the iTunes API. */
@Serializable
data class SearchResponse(
    @SerialName("resultCount")
    val resultCount: Int = 0,
    @SerialName("results")
    val results: List<Track> = emptyList()
)

/** Represents an audio track returned by the iTunes API. */
@Serializable
data class Track(
    @SerialName("wrapperType")
    val wrapperType: String? = null,
    @SerialName("kind")
    val kind: String? = null,
    @SerialName("collectionId")
    val collectionId: Long? = null,
    @SerialName("trackId")
    val trackId: Long = -1L,
    @SerialName("artistName")
    val artistName: String = "",
    @SerialName("collectionName")
    val collectionName: String? = null,
    @SerialName("trackName")
    val trackName: String = "",
    @SerialName("collectionCensoredName")
    val collectionCensoredName: String? = null,
    @SerialName("trackCensoredName")
    val trackCensoredName: String? = null,
    @SerialName("artistViewUrl")
    val artistViewUrl: String? = null,
    @SerialName("collectionViewUrl")
    val collectionViewUrl: String? = null,
    @SerialName("trackViewUrl")
    val trackViewUrl: String? = null,
    @SerialName("previewUrl")
    val previewUrl: String? = null,
    @SerialName("artworkUrl30")
    val artworkUrl30: String? = null,
    @SerialName("artworkUrl60")
    val artworkUrl60: String? = null,
    @SerialName("artworkUrl100")
    val artworkUrl100: String? = null,
    @SerialName("collectionPrice")
    val collectionPrice: Double? = null,
    @SerialName("trackPrice")
    val trackPrice: Double? = null,
    @SerialName("releaseDate")
    val releaseDate: String? = null,
    @SerialName("collectionExplicitness")
    val collectionExplicitness: String? = null,
    @SerialName("trackExplicitness")
    val trackExplicitness: String? = null,
    @SerialName("discNumber")
    val discNumber: Int? = null,
    @SerialName("trackNumber")
    val trackNumber: Int? = null,
    @SerialName("trackTimeMillis")
    val trackTimeMillis: Long? = null,
    @SerialName("country")
    val country: String? = null,
    @SerialName("currency")
    val currency: String? = null,
    @SerialName("primaryGenreName")
    val primaryGenreName: String? = null,
    @SerialName("isStreamable")
    val isStreamable: Boolean? = null,
    @SerialName("hasLyrics")
    val hasLyrics: Boolean? = null
)
