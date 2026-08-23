package dev.foss.goldenpath.inventory

import java.util.concurrent.ConcurrentHashMap

object RefreshHostBackoff {
    private val until = ConcurrentHashMap<String, Long>()

    fun note(host: String, delayMs: Long, nowMs: Long = System.currentTimeMillis()) {
        if (host.isBlank() || delayMs <= 0L) return
        until[host] = nowMs + delayMs
    }

    fun active(nowMs: Long = System.currentTimeMillis()): Map<String, Long> =
        until.mapValues { (it.value - nowMs).coerceAtLeast(0L) }.filterValues { it > 0L }

    fun clear() {
        until.clear()
    }
}
