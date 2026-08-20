package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexStore
import dev.foss.goldenpath.index.fdroid.FdroidRepo

object FdroidRefreshFetch {
    fun load(
        repo: FdroidRepo,
        fetcher: FdroidIndexFetcher,
        store: FdroidIndexStore?,
        nowMs: Long,
    ): ByteArray {
        val cached = store?.load(repo.id, nowMs)
        if (cached != null && cached.isNotEmpty()) {
            RefreshTrace.line("fdroid ${repo.id} cache ${cached.size}B")
            return cached
        }
        val raw = fetcher.fetch(repo.indexUrl).getOrElse { throw it }
        if (raw.isEmpty()) error("empty index")
        store?.save(repo.id, raw, nowMs)
        RefreshTrace.line("fdroid ${repo.id} downloaded ${raw.size}B")
        return raw
    }
}
