package dev.jmoicano.multiplayer.appauto

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.player.PlaylistNavigator
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository
import dev.jmoicano.multiplayer.core.player.playback.MediaPlayerFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Media service used by Android Auto for browsing, queue handling, and playback control.
 *
 * Centralizes media session, playlist, and playback state exposed to automotive clients.
 */
@AndroidEntryPoint
class MyMusicService : MediaBrowserServiceCompat() {

    companion object {
        private const val ROOT_ID = "root"
        private const val DEFAULT_QUERY = "Top"
        private const val PAGE_LIMIT = 50
        private const val SESSION_TAG = "MyMusicService"
        private const val DEFAULT_PLAYBACK_SPEED = 1.0f
        private const val UNKNOWN_POSITION = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
        private const val GENERIC_PLAYBACK_ERROR = "Nao foi possivel reproduzir esta musica"
        private const val LOG_TAG = "MPAutoService"

        private val SUPPORTED_ACTIONS =
            PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM or
                PlaybackStateCompat.ACTION_SEEK_TO
    }

    private lateinit var session: MediaSessionCompat
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    @Inject
    lateinit var searchRepository: SearchRepository
    @Inject
    lateinit var mediaPlayerFactory: MediaPlayerFactory

    private val playlistNavigator = PlaylistNavigator()

    private var mediaPlayer: MediaPlayer? = null

    @Volatile
    private var currentQuery: String = DEFAULT_QUERY

    @Volatile
    private var cachedTracks: List<Track> = emptyList()

    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            val currentTrack = playlistNavigator.currentOrNull() ?: playlistNavigator.firstOrNull()

            if (currentTrack == null) {
                updatePlaybackState(PlaybackStateCompat.STATE_NONE)
                return
            }

            val existingPlayer = mediaPlayer
            if (existingPlayer != null && !existingPlayer.isPlaying) {
                existingPlayer.start()
                session.isActive = true
                updatePlaybackState(
                    state = PlaybackStateCompat.STATE_PLAYING,
                    position = existingPlayer.currentPosition.toLong(),
                )
                return
            }

            playTrack(currentTrack)
        }

        override fun onSkipToQueueItem(queueId: Long) {
            val targetTrack = cachedTracks.getOrNull(queueId.toInt()) ?: return
            playlistNavigator.selectByMediaId(targetTrack.trackId.toString())
            playTrack(targetTrack)
        }

        override fun onSeekTo(position: Long) {
            val player = mediaPlayer ?: return
            val safePosition = position.coerceAtLeast(0L).toInt()
            player.seekTo(safePosition)
            updatePlaybackState(
                state = if (player.isPlaying) {
                    PlaybackStateCompat.STATE_PLAYING
                } else {
                    PlaybackStateCompat.STATE_PAUSED
                },
                position = safePosition.toLong(),
            )
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            val resolvedMediaId = mediaId ?: return

            val track = playlistNavigator.selectByMediaId(resolvedMediaId)
                ?: cachedTracks.firstOrNull { it.trackId.toString() == resolvedMediaId }?.also {
                    playlistNavigator.setPlaylist(cachedTracks)
                    playlistNavigator.selectByMediaId(resolvedMediaId)
                }

            if (track != null) {
                playTrack(track)
            }
        }

        override fun onPause() {
            val player = mediaPlayer ?: return
            if (player.isPlaying) {
                player.pause()
                updatePlaybackState(
                    state = PlaybackStateCompat.STATE_PAUSED,
                    position = player.currentPosition.toLong(),
                )
            }
        }

        override fun onStop() {
            stopPlayback(releasePlayer = true)
            session.isActive = false
            updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
        }

        override fun onSkipToNext() {
            val nextTrack = playlistNavigator.nextOrNull() ?: return
            playTrack(nextTrack)
        }

        override fun onSkipToPrevious() {
            val previousTrack = playlistNavigator.previousOrNull() ?: return
            playTrack(previousTrack)
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {}

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            currentQuery = query?.trim().takeUnless { it.isNullOrBlank() } ?: DEFAULT_QUERY
            notifyChildrenChanged(ROOT_ID)

            serviceScope.launch {
                val tracks = withContext(Dispatchers.IO) {
                    fetchTracks(currentQuery)
                }
                updatePlaylist(tracks)
                playlistNavigator.firstOrNull()?.let(::playTrack)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate")


        session = MediaSessionCompat(this, SESSION_TAG)
        sessionToken = session.sessionToken
        session.setCallback(callback)

        updatePlaybackState(PlaybackStateCompat.STATE_NONE)
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "onDestroy")
        serviceScope.cancel()
        stopPlayback(releasePlayer = true)
        session.release()
        super.onDestroy()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): MediaBrowserServiceCompat.BrowserRoot? {
        Log.d(LOG_TAG, "onGetRoot client=$clientPackageName uid=$clientUid")
        return MediaBrowserServiceCompat.BrowserRoot(ROOT_ID, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        Log.d(LOG_TAG, "onLoadChildren parentId=$parentId query=$currentQuery")
        if (parentId != ROOT_ID) {
            result.sendResult(mutableListOf())
            return
        }

        result.detach()
        serviceScope.launch {
            val tracks = withContext(Dispatchers.IO) {
                fetchTracks(currentQuery)
            }
            Log.d(LOG_TAG, "onLoadChildren fetched=${tracks.size}")
            updatePlaylist(tracks)
            val mediaItems = tracks.map { it.toMediaItem() }.toMutableList()
            result.sendResult(mediaItems)
        }
    }

    private suspend fun fetchTracks(query: String): List<Track> {
        Log.d(LOG_TAG, "fetchTracks query=$query")
        val result = searchRepository.searchTracks(
            query = query,
            limit = PAGE_LIMIT,
            offset = 0,
        )

        return result.getOrNull()?.results?.also {
            cachedTracks = it
            Log.d(LOG_TAG, "fetchTracks success=${it.size}")
        } ?: cachedTracks
    }

    private fun updatePlaylist(tracks: List<Track>) {
        cachedTracks = tracks
        playlistNavigator.setPlaylist(tracks)

        val queue = tracks.mapIndexed { index, track ->
            MediaSessionCompat.QueueItem(track.toMediaDescription(), index.toLong())
        }
        session.setQueue(queue)
    }

    private fun playTrack(track: Track) {
        val previewUrl = track.previewUrl
        if (previewUrl.isNullOrBlank()) {
            updatePlaybackState(
                state = PlaybackStateCompat.STATE_ERROR,
                errorMessage = GENERIC_PLAYBACK_ERROR,
            )
            return
        }

        val player = mediaPlayer ?: createMediaPlayer().also { mediaPlayer = it }

        try {
            player.reset()
            player.setDataSource(this, Uri.parse(previewUrl))
            player.setOnPreparedListener { preparedPlayer ->
                preparedPlayer.start()
                session.isActive = true
                updateMetadata(track, preparedPlayer.duration.toLong())
                updatePlaybackState(
                    state = PlaybackStateCompat.STATE_PLAYING,
                    position = preparedPlayer.currentPosition.toLong(),
                )
            }
            updateMetadata(track, track.trackTimeMillis)
            updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
            player.prepareAsync()
        } catch (_: Exception) {
            updatePlaybackState(
                state = PlaybackStateCompat.STATE_ERROR,
                errorMessage = GENERIC_PLAYBACK_ERROR,
            )
        }
    }

    private fun createMediaPlayer(): MediaPlayer {
        return mediaPlayerFactory.create().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener {
                callback.onSkipToNext()
            }
        }
    }

    private fun stopPlayback(releasePlayer: Boolean) {
        val player = mediaPlayer ?: return

        if (player.isPlaying) {
            player.stop()
        }

        if (releasePlayer) {
            player.release()
            mediaPlayer = null
        }
    }

    private fun updateMetadata(track: Track, duration: Long? = null) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.trackId.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.trackName)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artistName)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration ?: track.trackTimeMillis ?: 0L)
            .build()
        session.setMetadata(metadata)
    }

    private fun updatePlaybackState(
        state: Int,
        position: Long = UNKNOWN_POSITION,
        errorMessage: String? = null,
    ) {
        val builder = PlaybackStateCompat.Builder()
            .setActions(SUPPORTED_ACTIONS)
            .setState(state, position, DEFAULT_PLAYBACK_SPEED, SystemClock.elapsedRealtime())

        if (errorMessage != null) {
            builder.setErrorMessage(PlaybackStateCompat.ERROR_CODE_APP_ERROR, errorMessage)
        }

        session.setPlaybackState(builder.build())
    }

    private fun Track.toMediaDescription(): MediaDescriptionCompat {
        return MediaDescriptionCompat.Builder()
            .setMediaId(trackId.toString())
            .setTitle(trackName)
            .setSubtitle(artistName)
            .setIconUri(artworkUrl100?.let(Uri::parse))
            .setMediaUri(previewUrl?.let(Uri::parse))
            .build()
    }

    private fun Track.toMediaItem(): MediaItem {
        val description = toMediaDescription()
        return MediaItem(description, MediaItem.FLAG_PLAYABLE)
    }
}