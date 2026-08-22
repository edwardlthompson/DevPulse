package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidIndexBudget
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexStore
import dev.foss.goldenpath.index.fdroid.FdroidRepo

object FdroidRefreshFetch {
    fun load(
        repo: FdroidRepo,
        fetcher: FdroidIndexFetcher,
        store: FdroidIndexStore?,
        nowMs: Long,
        allowEmpty: Boolean = false,
    ): ByteArray {
        val cached = store?.load(repo.id, nowMs)
        if (cached != null && cached.size > FdroidIndexBudget.MAX_BYTES) {
            RefreshTrace.line("fdroid ${repo.id} skip ${cached.size}B over budget")
            store?.forget(repo.id)
        } else if (cached != null && cached.isNotEmpty()) {
            RefreshTrace.line("fdroid ${repo.id} cache ${cached.size}B")
            return cached
        }
        val raw = fetcher.fetch(repo.indexUrl).getOrElse { err ->
            val msg = err.message.orEmpty()
            if (allowEmpty && (msg.contains("over budget") || msg.contains("exceeds"))) {
                RefreshTrace.line("fdroid ${repo.id} skip ${err.message}")
                return ByteArray(0)
            }
            throw err
        }
        if (raw.isEmpty()) {
            if (allowEmpty) return raw
            error("empty index")
        }
        if (raw.size > FdroidIndexBudget.MAX_BYTES) {
            RefreshTrace.line("fdroid ${repo.id} skip ${raw.size}B over budget")
            if (allowEmpty) return ByteArray(0)
            error("index over budget")
        }
        store?.save(repo.id, raw, nowMs)
        RefreshTrace.line("fdroid ${repo.id} downloaded ${raw.size}B")
        return raw
    }
}
