package dev.foss.goldenpath.index.apkpure

import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource

object ApkPureScan {
    fun offersFor(
        packageNames: List<String>,
        fetcher: ApkPureBatchFetcher,
    ): Map<String, RemoteReleaseOffer> {
        val wanted = packageNames.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val out = linkedMapOf<String, RemoteReleaseOffer>()
        wanted.chunked(ApkPureFetchPolicy.CHUNK).forEach { chunk ->
            val fetched = fetcher.fetch(chunk)
            val body = fetched.getOrElse {
                RefreshTrace.line("apkpure chunk ${chunk.size} fail ${it.javaClass.simpleName}: ${it.message}")
                null
            }
            val parsed = if (body == null) emptyMap() else ApkPureMetaParser.parseMany(body)
            if (body != null) {
                RefreshTrace.line("apkpure chunk ${chunk.size} listed=${parsed.values.count { it.listed }}")
            }
            chunk.forEach { pkg ->
                out[pkg] = parsed[pkg] ?: RemoteReleaseOffer(
                    source = RemoteReleasedSource.ApkPure,
                    listed = false,
                    known = body != null,
                )
            }
        }
        return out
    }
}
