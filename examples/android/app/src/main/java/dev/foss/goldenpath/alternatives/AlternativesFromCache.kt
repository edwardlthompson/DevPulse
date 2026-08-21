package dev.foss.goldenpath.alternatives

import dev.foss.goldenpath.index.fdroid.FdroidPackageMeta
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.StalenessDays
import dev.foss.goldenpath.inventory.UpdateUrls
import dev.foss.goldenpath.staleness.Staleness

object AlternativesFromCache {
    fun hits(
        app: InstalledApp,
        inventory: List<InstalledApp>,
        meta: Map<String, FdroidPackageMeta>,
        nowMs: Long,
    ): List<AlternativeHit> {
        val days = StalenessDays.of(inventory, nowMs)
        val selfMeta = meta[app.packageName]
        val category = selfMeta?.category?.trim().orEmpty()
        val related = selfMeta?.related.orEmpty().toSet()
        val pool = inventory.filter { other ->
            other.packageName != app.packageName &&
                (other.packageName in related || (category.isNotEmpty() && meta[other.packageName]?.category == category))
        }
        val maintained = pool.filter { other ->
            val age = days[other.packageName] ?: return@filter false
            age < Staleness.AMBER_MAX_INCLUSIVE
        }
        val candidates = maintained.map { other ->
            AlternativeHit(
                packageName = other.packageName,
                title = other.label,
                score = if (other.packageName in related) 2 else 1,
                sourceUrl = other.latestListings.firstOrNull { !it.url.isNullOrBlank() }?.url
                    ?: UpdateUrls.forFdroid(other.packageName, "official", null),
            )
        }
        return AlternativesMatcher.match(app.label, candidates)
    }
}
