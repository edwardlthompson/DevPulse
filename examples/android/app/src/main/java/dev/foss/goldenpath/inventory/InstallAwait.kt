package dev.foss.goldenpath.inventory

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object InstallAwait {
    const val TIMEOUT_MS = 90_000L

    @Volatile
    private var latch: CountDownLatch? = null

    @Volatile
    private var ok: Boolean = false

    fun arm() {
        ok = false
        latch = CountDownLatch(1)
    }

    fun signal(success: Boolean) {
        ok = success
        latch?.countDown()
    }

    fun await(timeoutMs: Long = TIMEOUT_MS): Boolean {
        val gate = latch ?: return false
        val done = runCatching { gate.await(timeoutMs, TimeUnit.MILLISECONDS) }.getOrDefault(false)
        return done && ok
    }
}
