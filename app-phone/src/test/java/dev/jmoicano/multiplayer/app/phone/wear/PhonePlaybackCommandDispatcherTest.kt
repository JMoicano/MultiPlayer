package dev.jmoicano.multiplayer.app.phone.wear

import dev.jmoicano.multiplayer.core.player.sync.CrossDevicePlaybackCommand
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class PhonePlaybackCommandDispatcherTest {

    @After
    fun tearDown() {
        PhonePlaybackCommandDispatcher.resetForTests()
    }

    @Test
    fun dispatch_buffersCommandUntilHandlerRegisters() {
        val received = mutableListOf<CrossDevicePlaybackCommand>()
        val pendingCommand = CrossDevicePlaybackCommand(action = "pending")

        PhonePlaybackCommandDispatcher.dispatch(pendingCommand)
        PhonePlaybackCommandDispatcher.register(object : PhonePlaybackCommandDispatcher.Handler {
            override fun onWearPlaybackCommand(command: CrossDevicePlaybackCommand) {
                received += command
            }
        })

        assertEquals(listOf(pendingCommand), received)
    }

    @Test
    fun dispatch_deliversImmediatelyWhenHandlerIsRegistered() {
        val received = mutableListOf<CrossDevicePlaybackCommand>()
        val command = CrossDevicePlaybackCommand(action = "play")

        PhonePlaybackCommandDispatcher.register(object : PhonePlaybackCommandDispatcher.Handler {
            override fun onWearPlaybackCommand(command: CrossDevicePlaybackCommand) {
                received += command
            }
        })
        PhonePlaybackCommandDispatcher.dispatch(command)

        assertEquals(listOf(command), received)
    }
}

