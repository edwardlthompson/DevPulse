package dev.foss.goldenpath.inventory

object InventoryPresent {
    const val STALE_DAYS = 180

    fun visible(
        apps: List<InstalledApp>,
        includeSystem: Boolean,
        query: String,
        staleOnly: Boolean,
        updatesOnly: Boolean,
        githubOnly: Boolean = false,
        sourceFilters: Set<RemoteReleasedSource> = emptySet(),
        sortMode: InventorySortMode,
        usageByPackage: Map<String, UsageSnapshot>,
        nowMs: Long,
    ): List<InstalledApp> {
        val filtered = InventoryFilter.matchesQuery(
            InventoryFilter.visibleApps(apps, includeSystem),
            query,
        )
        val aged = if (staleOnly) {
            InventoryFilter.olderThan(filtered, STALE_DAYS, nowMs)
        } else {
            filtered
        }
        val updates = if (updatesOnly) UpdateInventory.withUpdates(aged) else aged
        val sources = if (githubOnly) sourceFilters + RemoteReleasedSource.Forge else sourceFilters
        val sourced = InventoryFilter.onAnyListedSource(updates, sources)
        return InventorySort.apply(sourced, sortMode, usageByPackage, nowMs)
    }
}
