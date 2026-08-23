package dev.foss.goldenpath.inventory

object UpdateAllPick {
    fun candidates(
        app: InstalledApp,
        deviceSdk: Int = 0,
        deviceAbis: Set<String> = emptySet(),
    ): List<UpdateAllJob> {
        val listed = UpdateInventory.usable(app, deviceSdk, deviceAbis)
        if (listed.isNotEmpty()) {
            return listed.sortedWith(::byNewest).map { link ->
                UpdateAllJob(app.packageName, app.label, link.source, link.url, link.versionName)
            }
        }
        if (app.latestListings.any { it.listed && VersionCompare.isNewer(it.versionName, app.versionName) }) {
            return emptyList()
        }
        UpdateArtifactMemory.best(app.packageName)?.takeUnless {
            IgnoredUpdates.has(app.packageName, it.source, it.versionName ?: app.remoteVersionName)
        }?.let {
            return listOf(
                UpdateAllJob(app.packageName, app.label, it.source, null, it.versionName ?: app.remoteVersionName),
            )
        }
        val source = app.remoteVersionSource
        if (!UpdateAll.fetchable(source, app.packageName)) return emptyList()
        if (IgnoredUpdates.has(app.packageName, source, app.remoteVersionName)) return emptyList()
        return listOf(UpdateAllJob(app.packageName, app.label, source, null, app.remoteVersionName))
    }

    fun groups(
        apps: List<InstalledApp>,
        selected: Set<String> = emptySet(),
        deviceSdk: Int = 0,
        deviceAbis: Set<String> = emptySet(),
    ): List<List<UpdateAllJob>> {
        val pool = if (selected.isEmpty()) apps else apps.filter { it.packageName in selected }
        return pool.filter(UpdateInventory::hasUpdate)
            .map { candidates(it, deviceSdk, deviceAbis) }
            .filter { it.isNotEmpty() }
            .take(UpdateAll.MAX_FILES)
    }

    private fun byNewest(left: UpdateLink, right: UpdateLink): Int {
        val compared = VersionCompare.compare(left.versionName.orEmpty(), right.versionName.orEmpty())
        if (compared != 0) return -compared
        return UpdateArtifactRank.rank(left.source).compareTo(UpdateArtifactRank.rank(right.source))
    }
}
