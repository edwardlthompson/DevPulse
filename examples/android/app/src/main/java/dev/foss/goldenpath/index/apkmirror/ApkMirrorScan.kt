package dev.foss.goldenpath.index.apkmirror

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
        val out = linkedMapOf<String, RemoteReleaseOffer>()
        wanted.chunked(ApkMirrorFetchPolicy.CHUNK).forEach { chunk ->
            val fetched = fetcher.fetch(chunk)
            val body = fetched.getOrElse {
                RefreshTrace.line("apkmirror chunk ${chunk.size} fail ${it.javaClass.simpleName}: ${it.message}")
                null
            }
            val parsed = if (body == null) emptyMap() else ApkMirrorMetaParser.parseMany(body, nowMs)
            if (body != null) {
                RefreshTrace.line("apkmirror chunk ${chunk.size} listed=${parsed.values.count { it.listed }}")
            }
            chunk.forEach { pkg ->
                out[pkg] = parsed[pkg] ?: RemoteReleaseOffer(
                    source = RemoteReleasedSource.ApkMirror,
                    listed = false,
                    known = body != null,
                )
            }
        }
        return out
    }
}
