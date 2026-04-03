package dev.jmoicano.multiplayer.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Abstracts the coroutine dispatchers used by the app to simplify dependency
 * injection and deterministic testing.
 */
interface CoroutineDispatchers {
    /** Dispatcher for network and disk operations. */
    val io: CoroutineDispatcher

    /** Dispatcher for CPU-bound work. */
    val default: CoroutineDispatcher

    /** Dispatcher for UI updates on the main thread. */
    val main: CoroutineDispatcher
}

/** Default implementation that maps directly to [Dispatchers]. */
object DefaultCoroutineDispatchers : CoroutineDispatchers {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
}

