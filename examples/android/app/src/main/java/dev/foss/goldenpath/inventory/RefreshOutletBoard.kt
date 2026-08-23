package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.forge.GitHubSearchPace
import java.util.concurrent.ConcurrentHashMap

data class RefreshOutletSnap(
    val id: String,
    val title: String,
    val done: Int,
    val total: Int,
    val current: String,
    val skipped: Boolean,
    val etaMs: Long?,
    val elapsedMs: Long = 0,
    val finishedAtMs: Long? = null,
)

object RefreshOutletIds {
    const val PLAY = "play"
    const val APTOIDE = "aptoide"
    const val GITHUB = "github"
    const val MIRROR = "apkmirror"
    const val PURE = "apkpure"
    const val LEFTOVER = "leftover"

    fun fdroid(repoId: String): String = "fdroid:$repoId"
}
object RefreshSkip {
    private val ids = ConcurrentHashMap.newKeySet<String>()
    fun reset() { ids.clear() }
    fun stop(id: String) { ids.add(id) }
    fun stopped(id: String): Boolean = id in ids
}

object RefreshOutletBoard {
    private val lock = Any()
    private val rows = linkedMapOf<String, Row>()

    fun reset() {
        synchronized(lock) { rows.clear() }
    }

    fun plan(id: String, title: String, total: Int, nowMs: Long = System.currentTimeMillis()) {
        synchronized(lock) {
            if (rows.containsKey(id)) return
            rows[id] = Row(title, total.coerceAtLeast(0), 0, "", nowMs)
        }
    }

    fun resize(id: String, total: Int, done: Int = 0) {
        synchronized(lock) {
            val row = rows[id] ?: return
            row.total = total.coerceAtLeast(0)
            row.done = done.coerceIn(0, row.total)
        }
    }

    fun fill(id: String, nowMs: Long = System.currentTimeMillis()) {
        synchronized(lock) { rows[id]?.seal(nowMs) }
    }

    fun at(id: String, current: String) {
        synchronized(lock) { rows[id]?.current = current }
    }

    fun tick(id: String, nowMs: Long = System.currentTimeMillis()) {
        synchronized(lock) {
            val row = rows[id] ?: return
            row.done = (row.done + 1).coerceAtMost(row.total.coerceAtLeast(row.done + 1))
            if (row.done >= row.total && row.total > 0) row.seal(nowMs)
        }
    }

    fun snaps(nowMs: Long = System.currentTimeMillis()): List<RefreshOutletSnap> = synchronized(lock) {
        rows.map { (id, row) ->
            val skipped = RefreshSkip.stopped(id)
            if (skipped) row.seal(nowMs)
            val remaining = (row.total - row.done).coerceAtLeast(0)
            val elapsed = row.elapsed(nowMs)
            RefreshOutletSnap(
                id = id,
                title = row.title,
                done = row.done,
                total = row.total,
                current = row.current,
                skipped = skipped,
                etaMs = if (skipped || remaining == 0) 0L else RefreshPaceBook.etaMs(id, remaining, row.done, elapsed),
                elapsedMs = elapsed,
                finishedAtMs = row.finishedAtMs,
            )
        }
    }

    fun noteFinished(id: String, nowMs: Long = System.currentTimeMillis()) {
        val row = synchronized(lock) { rows[id] } ?: return
        RefreshPaceBook.note(id, row.total.coerceAtLeast(1), (nowMs - row.t0).coerceAtLeast(1))
        RefreshResume.remember(id)
    }

    private data class Row(
        val title: String,
        var total: Int,
        var done: Int,
        var current: String,
        val t0: Long,
        var finishedAtMs: Long? = null,
    ) {
        fun seal(nowMs: Long) {
            done = total
            if (finishedAtMs == null) finishedAtMs = nowMs
        }

        fun elapsed(nowMs: Long): Long = ((finishedAtMs ?: nowMs) - t0).coerceAtLeast(0)
    }
}

object RefreshPaceBook {
    private val msPerItem = ConcurrentHashMap<String, Long>()

    fun hydrate(prior: Map<String, Long>) {
        prior.forEach { (id, ms) -> if (id.isNotBlank() && ms > 0) msPerItem[id] = ms }
    }

    fun snapshot(): Map<String, Long> = msPerItem.toMap()

    fun note(id: String, items: Int, elapsedMs: Long) {
        if (id.isBlank() || items <= 0 || elapsedMs <= 0) return
        msPerItem[id] = elapsedMs / items
    }

    fun etaMs(id: String, remaining: Int, done: Int, elapsedMs: Long): Long? {
        if (remaining <= 0) return 0L
        if (id == RefreshOutletIds.GITHUB) {
            return remaining * 60_000L / GitHubSearchPace.PER_MINUTE
        }
        if (done > 0 && elapsedMs > 0) return remaining.toLong() * elapsedMs / done
        val prior = msPerItem[id] ?: return null
        return remaining * prior
    }

    fun clear() {
        msPerItem.clear()
    }
}
