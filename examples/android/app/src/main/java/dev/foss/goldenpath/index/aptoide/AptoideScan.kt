package dev.foss.goldenpath.index.aptoide

import dev.foss.goldenpath.inventory.RemoteReleaseMemory
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasePick
import dev.foss.goldenpath.inventory.RemoteReleaseRollup
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifact
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import dev.foss.goldenpath.inventory.UpdateUrls

object AptoideScan {
    fun toPick(lookup: AptoideLookup, packageName: String = ""): RemoteReleasePick? {
        val ms = lookup.updatedOnMs ?: return null
        if (lookup.status != AptoideLookupStatus.Ok) return null
        val offer = RemoteReleaseOffer(
            source = RemoteReleasedSource.Aptoide,
            ms = ms,
            versionName = lookup.publishedVersion,
            pageUrl = packageName.takeIf { it.isNotEmpty() }?.let { UpdateUrls.aptoide(it, lookup.uname) },
        )
        if (packageName.isNotEmpty() && lookup.fileUrl != null) {
            UpdateArtifactMemory.add(
                UpdateArtifact(packageName, RemoteReleasedSource.Aptoide, lookup.fileUrl, lookup.publishedVersion),
            )
        }
        return RemoteReleaseRollup.from(listOf(offer))
    }

    fun lookupOne(
        packageName: String,
        fetcher: AptoideMetaFetcher,
        nowMs: Long,
        force: Boolean = false,
    ): AptoideLookup {
        if (!force) {
            AptoideLookupCache.getFresh(packageName, nowMs)?.let { return it }
        }
        val lookup = fetcher.fetch(packageName)
            .map { body -> AptoideMetaParser.parse(body, nowMs) }
            .getOrElse { AptoideLookup(null, null, AptoideLookupStatus.UnknownCheckManually) }
        AptoideLookupCache.put(packageName, lookup, nowMs)
        return lookup
    }

    fun picksFor(
        packageNames: List<String>,
        fetcher: AptoideMetaFetcher,
        nowMs: Long,
        sleepMs: (Long) -> Unit = { ms -> if (ms > 0) Thread.sleep(ms) },
        force: Boolean = false,
    ): Map<String, RemoteReleasePick> {
        val picks = linkedMapOf<String, RemoteReleasePick>()
        packageNames.forEachIndexed { index, packageName ->
            if (index > 0) sleepMs(AptoideFetchPolicy.MIN_INTERVAL_MS)
            val pick = toPick(lookupOne(packageName, fetcher, nowMs, force), packageName) ?: return@forEachIndexed
            picks[packageName] = pick
        }
        RemoteReleaseMemory.putAll(picks)
        return picks
    }
}
