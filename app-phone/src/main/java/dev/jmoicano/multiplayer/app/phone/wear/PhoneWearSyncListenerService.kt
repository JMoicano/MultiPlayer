package dev.jmoicano.multiplayer.app.phone.wear

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dev.jmoicano.multiplayer.app.phone.auto.MyMusicService
import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackProtocol
import dev.jmoicano.multiplayer.core.player.sync.SharedPlaybackStore

/** Receives Wear commands and forwards them to the phone playback service. */
class PhoneWearSyncListenerService : WearableListenerService() {

    companion object {
        private const val BOOTSTRAP_PUBLISH_DELAY_MS = 800L
    }

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    /** Processes watch messages filtered by the shared protocol path. */
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != CrossDevicePlaybackProtocol.COMMAND_MESSAGE_PATH) {
            super.onMessageReceived(messageEvent)
            return
        }

        val command = CrossDevicePlaybackProtocol.commandFromPayload(messageEvent.data) ?: return
        startService(Intent(this, MyMusicService::class.java))

        if (command.action == CrossDevicePlaybackProtocol.ACTION_REQUEST_STATE) {
            publishCurrentState()
            // Retry once after service bootstrap to avoid returning only an empty cold-start snapshot.
            mainHandler.postDelayed(::publishCurrentState, BOOTSTRAP_PUBLISH_DELAY_MS)
            return
        }

        PhonePlaybackCommandDispatcher.dispatch(command)
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    /** Publishes the current playback snapshot to the watch immediately. */
    private fun publishCurrentState() {
        PhoneWearStatePublisher.publish(applicationContext, SharedPlaybackStore.state.value)
    }
}
