package dev.foss.goldenpath.index.apkpure

import dev.foss.goldenpath.inventory.DumpChunkBook
import dev.foss.goldenpath.inventory.ListingMiss
import dev.foss.goldenpath.inventory.ProbeCache
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource

object ApkPureScan {
    fun offersFor(
        packageNames: List<String>,
        fetcher: ApkPureBatchFetcher,
        nowMs: Long,
    ): Map<String, RemoteReleaseOffer> {
        val wanted = packageNames.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val cached = wanted.associateWith {
            ProbeCache.fresh(it, RemoteReleasedSource.ApkPure, nowMs, ApkPureCachePolicy.TTL_MS)
        }
        val stale = wanted.filter { cached[it] == null }
        if (stale.isEmpty()) {
            RefreshTrace.line("apkpure cache ${wanted.size}")
            return cached.mapValues { it.value!! }
        }
        val out = linkedMapOf<String, RemoteReleaseOffer>()
        stale.chunked(ApkPureFetchPolicy.CHUNK).forEach { chunk ->
            val fetched = fetcher.fetch(chunk)
            val live = fetched.getOrElse {
                RefreshTrace.line("apkpure chunk ${chunk.size} fail ${it.javaClass.simpleName}: ${it.message}")
                null
            }
            if (live != null) DumpChunkBook.remember("apkpure", live)
            val body = live ?: DumpChunkBook.last("apkpure")
            val parsed = if (body == null) emptyMap() else ApkPureMetaParser.parseMany(body)
            if (body != null) {
                RefreshTrace.line("apkpure chunk ${chunk.size} listed=${parsed.values.count { it.listed }}")
            }
            chunk.forEach { pkg ->
                out[pkg] = ProbeCache.stamp(
                    parsed[pkg] ?: RemoteReleaseOffer(
                        source = RemoteReleasedSource.ApkPure,
                        listed = false,
                        known = body != null,
                        miss = when {
                            live == null && body == null -> ListingMiss.Parse
                            parsed[pkg] == null && body != null -> ListingMiss.Never
                            else -> null
                        },
                    ),
                    nowMs,
                )
            }
        }
        return wanted.associateWith { cached[it] ?: out.getValue(it) }
    }
}
