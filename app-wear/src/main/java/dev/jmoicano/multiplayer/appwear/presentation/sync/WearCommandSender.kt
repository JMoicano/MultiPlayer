package dev.jmoicano.multiplayer.appwear.presentation.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackCommand
import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackProtocol

/** Sends commands from watch to phone through the Wear Data Layer. */
object WearCommandSender {

    private const val LOG_TAG = "MPWearCommandSender"

    /** Sends a [command] to all connected nodes. */
    fun send(context: Context, command: CrossDevicePlaybackCommand) {
        val payload = CrossDevicePlaybackProtocol.commandToPayload(command)
        Wearable.getNodeClient(context).connectedNodes
            .addOnSuccessListener { nodes ->
                if (nodes.isEmpty()) {
                    Log.w(LOG_TAG, "No connected nodes for action=${command.action}")
                    return@addOnSuccessListener
                }

                nodes.forEach { node ->
                    Wearable.getMessageClient(context).sendMessage(
                        node.id,
                        CrossDevicePlaybackProtocol.COMMAND_MESSAGE_PATH,
                        payload,
                    ).addOnFailureListener { error ->
                        Log.e(LOG_TAG, "sendMessage failed action=${command.action} node=${node.id}", error)
                    }
                }
            }
            .addOnFailureListener { error ->
                Log.e(LOG_TAG, "connectedNodes failed action=${command.action}", error)
            }
    }

    /** Requests immediate state republishing from the phone. */
    fun requestState(context: Context) {
        send(
            context = context,
            command = CrossDevicePlaybackCommand(action = CrossDevicePlaybackProtocol.ACTION_REQUEST_STATE),
        )
    }
}
