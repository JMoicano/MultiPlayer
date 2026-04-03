package dev.jmoicano.multiplayer.app.phone.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackProtocol
import dev.jmoicano.multiplayer.core.player.sync.SharedPlaybackState

/** Publishes phone playback state into the Wear Data Layer. */
object PhoneWearStatePublisher {

    private const val LOG_TAG = "MPWearStatePublisher"

    /** Serializes and sends [state] to connected Wear nodes. */
    fun publish(context: Context, state: SharedPlaybackState) {
        val snapshot = CrossDevicePlaybackProtocol.toSnapshot(state)
        val request = PutDataMapRequest.create(CrossDevicePlaybackProtocol.STATE_DATA_PATH).apply {
            dataMap.putString("payload", CrossDevicePlaybackProtocol.snapshotToJson(snapshot))
            dataMap.putLong("publishedAtMs", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()

        Log.d(
            LOG_TAG,
            "publish state source=${state.source} playlist=${snapshot.playlist.size} browse=${snapshot.browseTracks.size} currentIndex=${snapshot.currentIndex} currentTrackId=${snapshot.playlist.getOrNull(snapshot.currentIndex)?.trackId}",
        )

        Wearable.getDataClient(context).putDataItem(request)
            .addOnSuccessListener { dataItem ->
                Log.d(LOG_TAG, "publish success uri=${dataItem.uri}")
            }
            .addOnFailureListener { error ->
                Log.e(LOG_TAG, "publish failed", error)
            }
    }
}
