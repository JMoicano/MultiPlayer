package dev.jmoicano.multiplayer.app.phone.auto

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import dev.jmoicano.multiplayer.app.phone.wear.PhonePlaybackCommandDispatcher
import dev.jmoicano.multiplayer.app.phone.wear.PhoneWearStatePublisher
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository
import dev.jmoicano.multiplayer.core.player.PlaylistNavigator
import dev.jmoicano.multiplayer.core.player.playback.MediaPlayerFactory
import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackCommand
import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackProtocol
import dev.jmoicano.multiplayer.core.player.sync.SharedPlaybackState
import dev.jmoicano.multiplayer.core.player.sync.SharedPlaybackStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phone media service serving Android Auto, local UI, and Wear synchronization.
 *
 * Maintains media session state, publishes shared playback state, and receives cross-device commands.
 */
@AndroidEntryPoint
class MyMusicService : MediaBrowserServiceCompat(), PhonePlaybackCommandDispatcher.Handler {

    companion object {
        private const val ROOT_ID = "root"
        private const val DEFAULT_QUERY = ""
        private const val PAGE_LIMIT = 50
        private const val SESSION_TAG = "PhoneMusicService"
        private const val DEFAULT_PLAYBACK_SPEED = 1.0f
        private const val UNKNOWN_POSITION = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
        private const val GENERIC_PLAYBACK_ERROR = "Nao foi possivel reproduzir esta musica"
        private const val LOG_TAG = "MPAutoPhoneService"
        private const val SYNC_SOURCE = "auto_phone_service"
        private const val ACTION_TOGGLE_LOOP = "dev.jmoicano.multiplayer.app.phone.action.TOGGLE_LOOP"
        private const val WEAR_PROGRESS_PUBLISH_THROTTLE_MS = 750L

        private val SUPPORTED_ACTIONS =
            PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_SET_REPEAT_MODE
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
    private var isPreparing: Boolean = false

    @Volatile
    private var isPrepared: Boolean = false

    @Volatile
    private var currentMediaId: String? = null

    @Volatile
    private var lastKnownPositionMs: Long = 0L

    @Volatile
    private var pendingStartPositionMs: Long = 0L

    @Volatile
    private var isLoopEnabled: Boolean = false

    @Volatile
    private var currentQuery: String = DEFAULT_QUERY

    @Volatile
    private var hasExplicitSearchRequest: Boolean = false

    @Volatile
    private var isApplyingSharedState: Boolean = false

    @Volatile
    private var lastBrowseUpdatedAtMs: Long = 0L

    @Volatile
    private var cachedTracks: List<Track> = emptyList()

    @Volatile
    private var lastWearPublishAtMs: Long = 0L

    @Volatile
    private var lastPublishedWearState: SharedPlaybackState? = null

    private val callback = object : MediaSessionCompat.Callback() {
         override fun onPlay() {
             val currentTrack = playlistNavigator.currentOrNull() ?: playlistNavigator.firstOrNull()

             if (currentTrack == null) {
                 Log.w(LOG_TAG, "No track to play")
                 updatePlaybackState(PlaybackStateCompat.STATE_NONE)
                 return
             }

             val existingPlayer = mediaPlayer
              if (existingPlayer != null && !existingPlayer.isPlaying && isPrepared) {
                 try {
                     existingPlayer.start()
                     session.isActive = true
                     Log.d(LOG_TAG, "Resumed playback")
                     updatePlaybackState(
                         state = PlaybackStateCompat.STATE_PLAYING,
                         position = existingPlayer.currentPosition.toLong(),
                     )
                      lastKnownPositionMs = existingPlayer.currentPosition.toLong()
                 } catch (ex: Exception) {
                     Log.e(LOG_TAG, "Error resuming playback: ${ex.message}", ex)
                      playTrack(currentTrack, resumePositionMs = lastKnownPositionMs)
                 }
                 return
             }

              if (existingPlayer != null && isPreparing) {
                  Log.d(LOG_TAG, "Player is preparing, ignoring duplicate play request")
                  updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
                  return
              }

              playTrack(currentTrack, resumePositionMs = pendingStartPositionMs)
         }

         override fun onSkipToQueueItem(queueId: Long) {
             val targetTrack = cachedTracks.getOrNull(queueId.toInt())
             if (targetTrack != null) {
                 Log.d(LOG_TAG, "Skipping to queue item $queueId: ${targetTrack.trackName}")
                 playlistNavigator.selectByMediaId(targetTrack.trackId.toString())
                 playTrack(targetTrack)
             } else {
                 Log.w(LOG_TAG, "Queue item $queueId not found")
             }
         }

         override fun onSeekTo(position: Long) {
             val player = mediaPlayer ?: return
             if (!isPrepared) {
                 Log.d(LOG_TAG, "Ignoring seek while player is not prepared")
                 return
             }
             try {
                 val safePosition = position.coerceAtLeast(0L).toInt()
                 player.seekTo(safePosition)
                 Log.d(LOG_TAG, "Seeked to ${safePosition}ms")
                 updatePlaybackState(
                     state = if (player.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                     position = safePosition.toLong(),
                 )
                  lastKnownPositionMs = safePosition.toLong()
                  publishSharedState(
                      isPlaying = player.isPlaying,
                      positionMs = safePosition.toLong(),
                      durationMs = player.duration.coerceAtLeast(0).toLong(),
                  )
             } catch (ex: Exception) {
                 Log.e(LOG_TAG, "Error seeking: ${ex.message}", ex)
             }
         }

          override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
               val resolvedMediaId = mediaId ?: return

               val track = playlistNavigator.selectByMediaId(resolvedMediaId)

               if (track != null) {
                   Log.d(LOG_TAG, "Playing from mediaId=$mediaId: ${track.trackName}")
                    val isSameTrack = currentMediaId == resolvedMediaId
                    val player = mediaPlayer
                    if (isSameTrack && player != null && isPrepared && !player.isPlaying) {
                        Log.d(LOG_TAG, "Same track prepared, resuming")
                        onPlay()
                    } else if (isSameTrack && isPreparing) {
                        Log.d(LOG_TAG, "Same track but still preparing")
                        updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
                    } else {
                        Log.d(LOG_TAG, "Starting playback of new track")
                        playTrack(track, resumePositionMs = if (isSameTrack) lastKnownPositionMs else 0L)
                    }
               } else {
                   Log.w(LOG_TAG, "Track not found for mediaId=$mediaId, cached=${cachedTracks.size} tracks")
               }
           }

         override fun onPause() {
             val player = mediaPlayer ?: return
              if (!isPrepared) return
             try {
                 if (player.isPlaying) {
                     player.pause()
                     Log.d(LOG_TAG, "Playback paused at ${player.currentPosition}ms")
                     updatePlaybackState(
                         state = PlaybackStateCompat.STATE_PAUSED,
                         position = player.currentPosition.toLong(),
                     )
                      lastKnownPositionMs = player.currentPosition.toLong()
                     publishSharedState(
                         isPlaying = false,
                         positionMs = player.currentPosition.toLong(),
                         durationMs = player.duration.coerceAtLeast(0).toLong(),
                     )
                 }
             } catch (ex: Exception) {
                 Log.e(LOG_TAG, "Error pausing: ${ex.message}", ex)
             }
         }

         override fun onStop() {
             try {
                 stopPlayback(releasePlayer = true)
                 session.isActive = false
                 Log.d(LOG_TAG, "Playback stopped")
                 updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
                  publishSharedState(
                      isPlaying = false,
                      positionMs = 0L,
                      durationMs = 0L,
                  )
             } catch (ex: Exception) {
                 Log.e(LOG_TAG, "Error stopping: ${ex.message}", ex)
             }
         }

         override fun onSkipToNext() {
             if (isLoopEnabled) {
                 playlistNavigator.currentOrNull()?.let {
                     playTrack(it, resumePositionMs = 0L)
                     return
                 }
             }
             val nextTrack = playlistNavigator.nextOrNull()
             if (nextTrack != null) {
                 Log.d(LOG_TAG, "Skipping to next: ${nextTrack.trackName}")
                 playTrack(nextTrack)
             } else {
                 Log.w(LOG_TAG, "No next track available")
             }
         }

         override fun onSkipToPrevious() {
             val previousTrack = playlistNavigator.previousOrNull()
             if (previousTrack != null) {
                 Log.d(LOG_TAG, "Skipping to previous: ${previousTrack.trackName}")
                 playTrack(previousTrack)
             } else {
                 Log.w(LOG_TAG, "No previous track available")
             }
         }

         override fun onSetRepeatMode(repeatMode: Int) {
             val enabled = repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE ||
                 repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL
             setLoopEnabled(enabled)
         }

         override fun onCustomAction(action: String?, extras: Bundle?) {
             if (action == ACTION_TOGGLE_LOOP) {
                 setLoopEnabled(!isLoopEnabled)
             }
         }

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            val normalizedQuery = query?.trim().takeUnless { it.isNullOrBlank() }
            if (normalizedQuery == null) {
                Log.d(LOG_TAG, "Ignoring empty search request")
                return
            }

            hasExplicitSearchRequest = true
            currentQuery = normalizedQuery
            Log.d(LOG_TAG, "onPlayFromSearch query=$currentQuery")
            notifyChildrenChanged(ROOT_ID)

            serviceScope.launch {
                val tracks = withContext(Dispatchers.IO) {
                    fetchTracks(currentQuery)
                }
                updatePlaylist(tracks)
                playlistNavigator.firstOrNull()?.let { playTrack(it) }
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
        PhonePlaybackCommandDispatcher.register(this)
        hydrateFromSharedState()
        publishWearState(SharedPlaybackStore.state.value)
        observeSharedState()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(LOG_TAG, "onBind action=${intent.action} package=${intent.`package`}")
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand action=${intent?.action} flags=$flags startId=$startId")
        return super.onStartCommand(intent, flags, startId)
    }

     override fun onDestroy() {
         Log.d(LOG_TAG, "onDestroy")
         try {
              PhonePlaybackCommandDispatcher.unregister(this)
             serviceScope.cancel()
             stopPlayback(releasePlayer = true)
             session.release()
         } catch (ex: Exception) {
             Log.e(LOG_TAG, "Error during onDestroy: ${ex.message}", ex)
         } finally {
             super.onDestroy()
         }
     }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        Log.d(LOG_TAG, "onGetRoot client=$clientPackageName uid=$clientUid")
        return BrowserRoot(ROOT_ID, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        Log.d(LOG_TAG, "onLoadChildren parentId=$parentId query=$currentQuery")
        if (parentId != ROOT_ID) {
            result.sendResult(mutableListOf())
            return
        }

        val browseTracks = SharedPlaybackStore.state.value.browseTracks
        if (browseTracks.isNotEmpty()) {
            Log.d(LOG_TAG, "onLoadChildren using synced browse snapshot size=${browseTracks.size}")
            updatePlaylist(browseTracks, publish = false)
            result.sendResult(browseTracks.map { it.toMediaItem() }.toMutableList())
            return
        }

        val syncedPlaylist = SharedPlaybackStore.state.value.playlist
        if (syncedPlaylist.isNotEmpty()) {
            Log.d(LOG_TAG, "onLoadChildren using synced playlist size=${syncedPlaylist.size}")
            updatePlaylist(syncedPlaylist, publish = false)
            result.sendResult(syncedPlaylist.map { it.toMediaItem() }.toMutableList())
            return
        }

        if (!hasExplicitSearchRequest) {
            Log.d(LOG_TAG, "onLoadChildren no explicit search yet, returning empty list")
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
            result.sendResult(tracks.map { it.toMediaItem() }.toMutableList())
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

    private fun updatePlaylist(tracks: List<Track>, publish: Boolean = true) {
        cachedTracks = tracks
        playlistNavigator.setPlaylist(tracks)
        session.setQueue(
            tracks.mapIndexed { index, track ->
                MediaSessionCompat.QueueItem(track.toMediaDescription(), index.toLong())
            }
        )
        if (publish) {
            publishSharedState(
                isPlaying = mediaPlayer?.isPlaying == true,
                positionMs = mediaPlayer?.currentPosition?.toLong() ?: 0L,
                durationMs = mediaPlayer?.duration?.coerceAtLeast(0)?.toLong() ?: 0L,
            )
        }
    }

     private fun playTrack(track: Track, resumePositionMs: Long = 0L) {
         val previewUrl = track.previewUrl
         if (previewUrl.isNullOrBlank()) {
             Log.w(LOG_TAG, "Track has no preview URL: ${track.trackId}")
             updatePlaybackState(
                 state = PlaybackStateCompat.STATE_ERROR,
                 errorMessage = GENERIC_PLAYBACK_ERROR,
             )
             return
         }

         val player = mediaPlayer ?: createMediaPlayer().also { mediaPlayer = it }

         try {
             Log.d(LOG_TAG, "Playing track: ${track.trackName} by ${track.artistName}")
             player.reset()
              isPreparing = true
              isPrepared = false
              currentMediaId = track.trackId.toString()
              pendingStartPositionMs = resumePositionMs.coerceAtLeast(0L)
              // Use String data source for network URLs to avoid ContentResolver lookup failures.
              player.setDataSource(previewUrl)
             player.setOnPreparedListener { preparedPlayer ->
                 Log.d(LOG_TAG, "Track prepared, duration=${preparedPlayer.duration}ms")
                  isPreparing = false
                  isPrepared = true
                  val startPosition = pendingStartPositionMs.coerceAtLeast(0L)
                  if (startPosition > 0L) {
                      preparedPlayer.seekTo(startPosition.toInt())
                  }
                 preparedPlayer.start()
                  lastKnownPositionMs = preparedPlayer.currentPosition.toLong()
                  pendingStartPositionMs = 0L
                 session.isActive = true
                 updateMetadata(track, preparedPlayer.duration.toLong())
                 updatePlaybackState(
                     state = PlaybackStateCompat.STATE_PLAYING,
                     position = preparedPlayer.currentPosition.toLong(),
                 )
                  publishSharedState(
                      isPlaying = true,
                      positionMs = preparedPlayer.currentPosition.toLong(),
                      durationMs = preparedPlayer.duration.coerceAtLeast(0).toLong(),
                  )
             }
             updateMetadata(track, track.trackTimeMillis)
             updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
              publishSharedState(
                  isPlaying = false,
                  positionMs = 0L,
                  durationMs = track.trackTimeMillis ?: 0L,
              )
             player.prepareAsync()
         } catch (ex: Exception) {
              isPreparing = false
              isPrepared = false
              pendingStartPositionMs = 0L
             Log.e(LOG_TAG, "Error preparing track: ${ex.message}", ex)
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
             setOnCompletionListener { completedPlayer ->
                 // Skip to next only if player is still valid and not null
                 if (completedPlayer == mediaPlayer) {
                      isPreparing = false
                      isPrepared = false
                      lastKnownPositionMs = 0L
                      if (isLoopEnabled) {
                          val current = playlistNavigator.currentOrNull()
                          if (current != null) {
                              Log.d(LOG_TAG, "Track completed, replaying due to loop")
                              playTrack(current, resumePositionMs = 0L)
                          }
                      } else {
                          Log.d(LOG_TAG, "Track completed, skipping to next")
                          callback.onSkipToNext()
                      }
                 }
             }
             setOnErrorListener { errorPlayer, what, extra ->
                  isPreparing = false
                  isPrepared = false
                  pendingStartPositionMs = 0L
                 Log.e(LOG_TAG, "MediaPlayer error: what=$what extra=$extra")
                 updatePlaybackState(
                     state = PlaybackStateCompat.STATE_ERROR,
                     errorMessage = GENERIC_PLAYBACK_ERROR,
                 )
                 true // Consumed
             }
         }
     }

     private fun stopPlayback(releasePlayer: Boolean) {
         val player = mediaPlayer ?: return
         try {
              isPreparing = false
              isPrepared = false
              pendingStartPositionMs = 0L
             if (player.isPlaying) {
                 player.stop()
             }
             if (releasePlayer) {
                 player.release()
                 mediaPlayer = null
                  currentMediaId = null
                  lastKnownPositionMs = 0L
                 Log.d(LOG_TAG, "MediaPlayer released")
             }
         } catch (ex: Exception) {
             Log.e(LOG_TAG, "Error stopping playback: ${ex.message}", ex)
             mediaPlayer = null
         }
     }

    private fun updateMetadata(track: Track, duration: Long? = null) {
        val artwork = track.artworkUrl100
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.trackId.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.trackName)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artistName)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.collectionName)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration ?: track.trackTimeMillis ?: 0L)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, artwork)
            .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, artwork)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, artwork)
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
            .addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    ACTION_TOGGLE_LOOP,
                    if (isLoopEnabled) "Loop ON" else "Loop OFF",
                    dev.jmoicano.multiplayer.core.designsystem.R.drawable.ic_play_on_repeat,
                ).build()
            )

        if (errorMessage != null) {
            builder.setErrorMessage(PlaybackStateCompat.ERROR_CODE_APP_ERROR, errorMessage)
        }

        session.setPlaybackState(builder.build())
    }

    private fun hydrateFromSharedState() {
        val synced = SharedPlaybackStore.state.value
        val syncedTracks = synced.playlist.ifEmpty { synced.browseTracks }
        if (syncedTracks.isEmpty()) return

        currentQuery = synced.query
        hasExplicitSearchRequest = synced.query.isNotBlank()
        updatePlaylist(syncedTracks, publish = false)
        val selectedIndex = when {
            synced.playlist.isNotEmpty() -> synced.currentIndex
            synced.currentIndex in syncedTracks.indices -> synced.currentIndex
            else -> -1
        }
        if (selectedIndex >= 0) {
            playlistNavigator.selectByIndex(selectedIndex)
        }
        currentMediaId = synced.playlist.getOrNull(synced.currentIndex)?.trackId?.toString()
        lastKnownPositionMs = synced.positionMs
        pendingStartPositionMs = synced.positionMs
        isLoopEnabled = synced.isLoopEnabled
        session.setRepeatMode(if (isLoopEnabled) PlaybackStateCompat.REPEAT_MODE_ONE else PlaybackStateCompat.REPEAT_MODE_NONE)
        synced.playlist.getOrNull(synced.currentIndex)?.let { track ->
            updateMetadata(track, synced.durationMs.takeIf { it > 0L } ?: track.trackTimeMillis)
        }
        updatePlaybackState(
            state = if (synced.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
            position = synced.positionMs,
        )
    }

    private fun publishSharedState(
        isPlaying: Boolean,
        positionMs: Long,
        durationMs: Long,
    ) {
        if (isApplyingSharedState) return
        SharedPlaybackStore.publish(
            source = SYNC_SOURCE,
            playlist = cachedTracks,
            currentIndex = playlistNavigator.currentIndex(),
            isPlaying = isPlaying,
            isLoopEnabled = isLoopEnabled,
            positionMs = positionMs,
            durationMs = durationMs,
            query = currentQuery,
        )
    }

    private fun setLoopEnabled(enabled: Boolean) {
        isLoopEnabled = enabled
        session.setRepeatMode(if (enabled) PlaybackStateCompat.REPEAT_MODE_ONE else PlaybackStateCompat.REPEAT_MODE_NONE)
        updatePlaybackState(
            state = if (mediaPlayer?.isPlaying == true) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
            position = mediaPlayer?.currentPosition?.toLong() ?: lastKnownPositionMs,
        )
        publishSharedState(
            isPlaying = mediaPlayer?.isPlaying == true,
            positionMs = mediaPlayer?.currentPosition?.toLong() ?: lastKnownPositionMs,
            durationMs = mediaPlayer?.duration?.coerceAtLeast(0)?.toLong() ?: 0L,
        )
    }

    private fun observeSharedState() {
        serviceScope.launch {
            SharedPlaybackStore.state.collectLatest { sharedState ->
                publishWearState(sharedState)

                if (sharedState.browseUpdatedAtMs > lastBrowseUpdatedAtMs) {
                    lastBrowseUpdatedAtMs = sharedState.browseUpdatedAtMs
                    notifyChildrenChanged(ROOT_ID)
                }

                if (sharedState.source == SYNC_SOURCE || sharedState.playlist.isEmpty()) return@collectLatest

                isApplyingSharedState = true
                try {
                    currentQuery = sharedState.query
                    hasExplicitSearchRequest = sharedState.query.isNotBlank()
                    updatePlaylist(sharedState.playlist, publish = false)
                    playlistNavigator.selectByIndex(sharedState.currentIndex)
                    currentMediaId = sharedState.currentTrack?.trackId?.toString()
                    lastKnownPositionMs = sharedState.positionMs
                    pendingStartPositionMs = sharedState.positionMs
                    isLoopEnabled = sharedState.isLoopEnabled
                    session.setRepeatMode(if (isLoopEnabled) PlaybackStateCompat.REPEAT_MODE_ONE else PlaybackStateCompat.REPEAT_MODE_NONE)
                    sharedState.currentTrack?.let { track ->
                        updateMetadata(track, sharedState.durationMs.takeIf { it > 0L } ?: track.trackTimeMillis)
                    }
                    updatePlaybackState(
                        state = if (sharedState.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                        position = sharedState.positionMs,
                    )
                } finally {
                    isApplyingSharedState = false
                }
            }
        }
    }

    override fun onWearPlaybackCommand(command: CrossDevicePlaybackCommand) {
        Log.d(LOG_TAG, "Wear command received: ${command.action}")
        when (command.action) {
            CrossDevicePlaybackProtocol.ACTION_TOGGLE_PLAY_PAUSE -> {
                Log.d(LOG_TAG, "Wear command: toggle play/pause, isPlaying=${mediaPlayer?.isPlaying}")
                if (mediaPlayer?.isPlaying == true) callback.onPause() else callback.onPlay()
            }
            CrossDevicePlaybackProtocol.ACTION_PLAY -> {
                Log.d(LOG_TAG, "Wear command: play")
                callback.onPlay()
            }
            CrossDevicePlaybackProtocol.ACTION_PAUSE -> {
                Log.d(LOG_TAG, "Wear command: pause")
                callback.onPause()
            }
            CrossDevicePlaybackProtocol.ACTION_NEXT -> {
                Log.d(LOG_TAG, "Wear command: next")
                callback.onSkipToNext()
            }
            CrossDevicePlaybackProtocol.ACTION_PREVIOUS -> {
                Log.d(LOG_TAG, "Wear command: previous")
                callback.onSkipToPrevious()
            }
            CrossDevicePlaybackProtocol.ACTION_PLAY_TRACK -> {
                command.trackId?.let { trackId ->
                    Log.d(LOG_TAG, "Wear command: play track $trackId")
                    callback.onPlayFromMediaId(trackId.toString(), null)
                }
            }
            CrossDevicePlaybackProtocol.ACTION_SEEK_TO -> {
                command.positionMs?.let {
                    Log.d(LOG_TAG, "Wear command: seek to ${it}ms")
                    callback.onSeekTo(it)
                }
            }
            CrossDevicePlaybackProtocol.ACTION_SET_LOOP -> {
                command.loopEnabled?.let {
                    Log.d(LOG_TAG, "Wear command: set loop to $it")
                    setLoopEnabled(it)
                }
            }
        }
    }

    private fun publishWearState(sharedState: SharedPlaybackState) {
        val now = System.currentTimeMillis()
        val previous = lastPublishedWearState
        val hasStructuralChange = previous == null || hasWearStructuralChange(previous, sharedState)

        if (!hasStructuralChange && now - lastWearPublishAtMs < WEAR_PROGRESS_PUBLISH_THROTTLE_MS) {
            Log.d(
                LOG_TAG,
                "publishWearState skipped by throttle dt=${now - lastWearPublishAtMs}ms playlist=${sharedState.playlist.size} browse=${sharedState.browseTracks.size} index=${sharedState.currentIndex}",
            )
            return
        }

        val publishReason = if (hasStructuralChange) "structural_change" else "progress_tick"
        Log.d(
            LOG_TAG,
            "publishWearState reason=$publishReason source=${sharedState.source} playlist=${sharedState.playlist.size} browse=${sharedState.browseTracks.size} index=${sharedState.currentIndex} currentTrackId=${sharedState.currentTrack?.trackId} isPlaying=${sharedState.isPlaying}",
        )

        lastWearPublishAtMs = now
        lastPublishedWearState = sharedState
        PhoneWearStatePublisher.publish(applicationContext, sharedState)
    }

    private fun hasWearStructuralChange(previous: SharedPlaybackState, current: SharedPlaybackState): Boolean {
        if (previous.isPlaying != current.isPlaying) return true
        if (previous.isLoopEnabled != current.isLoopEnabled) return true
        if (previous.currentIndex != current.currentIndex) return true
        if (previous.query != current.query) return true
        if (previous.browseQuery != current.browseQuery) return true
        if (previous.playlist.trackIds() != current.playlist.trackIds()) return true
        if (previous.browseTracks.trackIds() != current.browseTracks.trackIds()) return true
        return false
    }

    private fun List<Track>.trackIds(): List<Long> = map { it.trackId }

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
        return MediaItem(toMediaDescription(), MediaItem.FLAG_PLAYABLE)
    }
}



