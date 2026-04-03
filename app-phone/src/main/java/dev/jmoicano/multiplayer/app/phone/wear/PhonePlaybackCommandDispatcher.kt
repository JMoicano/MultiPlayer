package dev.jmoicano.multiplayer.app.phone.wear

import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackCommand

object PhonePlaybackCommandDispatcher {

    interface Handler {
        fun onWearPlaybackCommand(command: CrossDevicePlaybackCommand)
    }

    @Volatile
    private var handler: Handler? = null
    private val pendingCommands = ArrayDeque<CrossDevicePlaybackCommand>()

    @Synchronized
    fun register(handler: Handler) {
        this.handler = handler
        while (pendingCommands.isNotEmpty()) {
            handler.onWearPlaybackCommand(pendingCommands.removeFirst())
        }
    }

    @Synchronized
    fun unregister(handler: Handler) {
        if (this.handler == handler) {
            this.handler = null
        }
    }

    @Synchronized
    fun dispatch(command: CrossDevicePlaybackCommand) {
        val activeHandler = handler
        if (activeHandler == null) {
            pendingCommands.addLast(command)
            return
        }
        activeHandler.onWearPlaybackCommand(command)
    }

    @Synchronized
    internal fun resetForTests() {
        handler = null
        pendingCommands.clear()
    }
}
