package dev.foss.goldenpath.inventory

enum class InventorySortMode {
    Oldest,
    Newest,
    Name,
    UsedAndStale,
}

object InventorySort {
    fun apply(
        apps: List<InstalledApp>,
        mode: InventorySortMode,
        usageByPackage: Map<String, UsageSnapshot> = emptyMap(),
        nowMs: Long,
    ): List<InstalledApp> = when (mode) {
        InventorySortMode.Name -> InventoryFilter.sortedByLabel(apps)
        InventorySortMode.Oldest -> apps.sortedWith(ageComparator(unknownLast = true, newestFirst = false))
        InventorySortMode.Newest -> apps.sortedWith(ageComparator(unknownLast = true, newestFirst = true))
        InventorySortMode.UsedAndStale ->
            if (usageByPackage.isEmpty()) {
                apply(apps, InventorySortMode.Oldest, emptyMap(), nowMs)
            } else {
                apps.sortedWith(
                    compareByDescending<InstalledApp> { app ->
                        UsagePulse.score(app, usageByPackage[app.packageName], nowMs)
                    }.thenBy { it.label.lowercase() },
                )
            }
    }

    private fun ageComparator(unknownLast: Boolean, newestFirst: Boolean): Comparator<InstalledApp> =
        compareBy<InstalledApp> { app ->
            val unknown = RemoteRelease.ageMs(app) == null
            if (unknownLast && unknown) 1 else 0
        }.thenBy { app ->
            val age = RemoteRelease.ageMs(app) ?: 0L
            if (newestFirst) -age else age
        }
}
