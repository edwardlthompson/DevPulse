package dev.foss.goldenpath.index.apkmirror

import dev.foss.goldenpath.inventory.DumpChunkBook
import dev.foss.goldenpath.inventory.ListingMiss
import dev.foss.goldenpath.inventory.ProbeCache
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource

object ApkMirrorScan {
    fun offersFor(
        packageNames: List<String>,
        fetcher: ApkMirrorBatchFetcher,
        nowMs: Long,
    ): Map<String, RemoteReleaseOffer> {
        val wanted = packageNames.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val cached = wanted.associateWith {
            ProbeCache.fresh(it, RemoteReleasedSource.ApkMirror, nowMs, ApkMirrorCachePolicy.TTL_MS)
        }
        val stale = wanted.filter { cached[it] == null }
        if (stale.isEmpty()) {
            RefreshTrace.line("apkmirror cache ${wanted.size}")
            return cached.mapValues { it.value!! }
        }
        val out = linkedMapOf<String, RemoteReleaseOffer>()
        fetchChunks(stale.chunked(ApkMirrorFetchPolicy.CHUNK), fetcher).forEach { (chunk, fetched) ->
            val live = fetched.getOrElse {
                RefreshTrace.line("apkmirror chunk ${chunk.size} fail ${it.javaClass.simpleName}: ${it.message}")
                null
            }
            if (live != null) DumpChunkBook.remember("apkmirror", live)
            val body = live ?: DumpChunkBook.last("apkmirror")
            val parsed = if (body == null) emptyMap() else ApkMirrorMetaParser.parseMany(body, nowMs)
            if (body != null) {
                RefreshTrace.line("apkmirror chunk ${chunk.size} listed=${parsed.values.count { it.listed }}")
            }
            chunk.forEach { pkg ->
                out[pkg] = ProbeCache.stamp(
                    parsed[pkg] ?: RemoteReleaseOffer(
                        source = RemoteReleasedSource.ApkMirror,
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

    private fun fetchChunks(
        chunks: List<List<String>>,
        fetcher: ApkMirrorBatchFetcher,
    ): List<Pair<List<String>, Result<String>>> {
        if (chunks.size <= 1) return chunks.map { it to fetcher.fetch(it) }
        val pool = java.util.concurrent.Executors.newFixedThreadPool(
            chunks.size.coerceAtMost(ApkMirrorFetchPolicy.PARALLEL),
        )
        return try {
            val jobs = chunks.map { chunk -> pool.submit<Result<String>> { fetcher.fetch(chunk) } }
            chunks.zip(jobs.map { it.get() })
        } finally {
            pool.shutdown()
        }
    }
}
