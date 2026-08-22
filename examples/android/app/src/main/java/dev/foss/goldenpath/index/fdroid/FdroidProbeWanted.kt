package dev.foss.goldenpath.index.fdroid

import dev.foss.goldenpath.inventory.ListingChannels
import dev.foss.goldenpath.inventory.ProbeCache
import dev.foss.goldenpath.inventory.RefreshTrace

data class FdroidProbeSlice(
    val stale: Set<String>,
    val cached: List<FdroidAppRecord>,
)

object FdroidProbeWanted {
    fun slice(
        repo: FdroidRepo,
        wanted: Set<String>,
        nowMs: Long,
        names: FdroidNameCatalog?,
    ): FdroidProbeSlice {
        val cataloged = names?.probe(repo.id, wanted) ?: wanted
        if (names != null && names.loaded(repo.id)) {
            RefreshTrace.line("fdroid ${repo.id} catalog ${cataloged.size}/${wanted.size}")
        }
        val source = ListingChannels.sourceForRepo(repo.id)
        val cached = cataloged.mapNotNull { pkg ->
            ProbeCache.fresh(pkg, source, nowMs, FdroidCachePolicy.TTL_MS)?.takeIf { it.listed }?.let { offer ->
                FdroidAppRecord(pkg, offer.ms, offer.pageUrl, repo.id, offer.versionName)
            }
        }
        val stale = cataloged.filterTo(linkedSetOf()) { pkg ->
            ProbeCache.fresh(pkg, source, nowMs, FdroidCachePolicy.TTL_MS) == null
        }
        return FdroidProbeSlice(stale, cached)
    }
}
