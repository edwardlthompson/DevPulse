package dev.foss.goldenpath.inventory

import java.util.concurrent.ConcurrentHashMap

object RefreshSuccessBook {
    private val at = ConcurrentHashMap<String, Long>()

    fun hydrate(prior: Map<String, Long>) {
        prior.forEach { (id, ms) -> if (id.isNotBlank() && ms > 0L) at[id] = ms }
    }

    fun snapshot(): Map<String, Long> = at.toMap()

    fun note(id: String, atMs: Long) {
        if (id.isBlank() || atMs <= 0L) return
        at[id] = atMs
    }

    fun capture(outlets: List<RefreshOutletSnap>) {
        outlets.forEach { snap -> snap.finishedAtMs?.let { note(snap.id, it) } }
    }

    fun clear() {
        at.clear()
    }
}
