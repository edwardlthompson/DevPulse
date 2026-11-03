package dev.foss.goldenpath.inventory

import java.util.concurrent.ConcurrentHashMap

object RefreshSkip {
    private val ids = ConcurrentHashMap.newKeySet<String>()

    fun reset() {
        ids.clear()
    }

    fun stop(id: String) {
        ids.add(id)
    }

    fun stopAll() {
        listOf(
            RefreshOutletIds.PLAY,
            RefreshOutletIds.APTOIDE,
            RefreshOutletIds.GITHUB,
            RefreshOutletIds.MIRROR,
            RefreshOutletIds.PURE,
            RefreshOutletIds.LEFTOVER,
        ).forEach(::stop)
    }

    fun stopped(id: String): Boolean = id in ids
}
