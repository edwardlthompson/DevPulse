package dev.foss.goldenpath.inventory

object InventoryFilter {
    fun visibleApps(apps: List<InstalledApp>, includeSystem: Boolean): List<InstalledApp> =
        if (includeSystem) apps else apps.filter { !it.isSystemApp }

    fun sortedByLabel(apps: List<InstalledApp>): List<InstalledApp> =
        apps.sortedBy { it.label.lowercase() }

    fun matchesQuery(apps: List<InstalledApp>, query: String): List<InstalledApp> {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return apps
        return apps.filter { app ->
            app.label.lowercase().contains(needle) || app.packageName.lowercase().contains(needle)
        }
    }

    fun olderThan(apps: List<InstalledApp>, minAgeDays: Int, nowMs: Long): List<InstalledApp> =
        apps.filter { app ->
            val ageMs = RemoteRelease.ageMs(app) ?: return@filter false
            (nowMs - ageMs) / 86_400_000L >= minAgeDays
        }

    fun onGitHub(apps: List<InstalledApp>): List<InstalledApp> =
        onListedSource(apps, RemoteReleasedSource.Forge)

    fun onListedSource(apps: List<InstalledApp>, source: RemoteReleasedSource): List<InstalledApp> =
        onAnyListedSource(apps, setOf(source))

    fun onAnyListedSource(
        apps: List<InstalledApp>,
        sources: Set<RemoteReleasedSource>,
    ): List<InstalledApp> {
        if (sources.isEmpty()) return apps
        return apps.filter { app ->
            app.latestListings.any { listing ->
                listing.source in sources && listing.listed && listing.known
            }
        }
    }
}
