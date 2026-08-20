package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidAppRecord

object FdroidIndexMisses {
    fun offers(
        wanted: Set<String>,
        okRepoIds: Set<String>,
        hits: List<FdroidAppRecord>,
    ): Map<String, List<RemoteReleaseOffer>> {
        val found = hits.groupBy { it.repoId }.mapValues { entry ->
            entry.value.map { it.packageName }.toSet()
        }
        return wanted.associateWith { pkg ->
            okRepoIds.mapNotNull { repoId ->
                if (pkg in found[repoId].orEmpty()) {
                    null
                } else {
                    RemoteReleaseOffer(ListingChannels.sourceForRepo(repoId), listed = false, known = true)
                }
            }
        }
    }

    fun merge(
        hits: Map<String, List<RemoteReleaseOffer>>,
        misses: Map<String, List<RemoteReleaseOffer>>,
    ): Map<String, List<RemoteReleaseOffer>> {
        val keys = hits.keys + misses.keys
        return keys.associateWith { pkg -> hits[pkg].orEmpty() + misses[pkg].orEmpty() }
    }
}
