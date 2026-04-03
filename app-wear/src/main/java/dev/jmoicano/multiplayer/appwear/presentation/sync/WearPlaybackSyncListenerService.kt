package dev.jmoicano.multiplayer.appwear.presentation.sync

import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackProtocol

/** Receives playback snapshots published by the phone via Wear Data Layer. */
class WearPlaybackSyncListenerService : WearableListenerService() {

    companion object {
        private const val LOG_TAG = "MPWearSyncListener"
    }

    /** Updates local store whenever an item on the state path changes. */
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { dataEvent ->
            val item = dataEvent.dataItem
            if (item.uri.path != CrossDevicePlaybackProtocol.STATE_DATA_PATH) return@forEach

            val payload = DataMapItem.fromDataItem(item).dataMap.getString("payload").orEmpty()
            if (payload.isBlank()) {
                Log.w(LOG_TAG, "onDataChanged blank payload uri=${item.uri}")
                return@forEach
            }

            val snapshot = CrossDevicePlaybackProtocol.snapshotFromJson(payload)
            if (snapshot == null) {
                Log.e(LOG_TAG, "onDataChanged invalid payload uri=${item.uri} payloadSize=${payload.length}")
                return@forEach
            }

            Log.d(
                LOG_TAG,
                "onDataChanged uri=${item.uri} payloadSize=${payload.length} playlist=${snapshot.playlist.size} browse=${snapshot.browseTracks.size} index=${snapshot.currentIndex} currentTrackId=${snapshot.playlist.getOrNull(snapshot.currentIndex)?.trackId}",
            )
            WearPlaybackSyncStore.update(snapshot)
        }
    }
}
