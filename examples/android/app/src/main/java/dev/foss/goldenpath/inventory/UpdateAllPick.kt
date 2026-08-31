package dev.foss.goldenpath.inventory

object UpdateAllPick {
    fun candidates(
        app: InstalledApp,
        deviceSdk: Int = 0,
        deviceAbis: Set<String> = emptySet(),
        auroraPlay: Boolean = true,
    ): List<UpdateAllJob> {
        val listed = UpdateInventory.usable(app, deviceSdk, deviceAbis)
            .filter { it.source != RemoteReleasedSource.Play || auroraPlay }
        if (listed.isNotEmpty()) {
            val jobs = listed.sortedWith(::byNewest).map { link ->
                UpdateAllJob(app.packageName, app.label, link.source, link.url, link.versionName)
            }.toMutableList()
            val seenSources = jobs.map { it.source }.toSet()
            UpdateArtifactMemory.byPackage[app.packageName]?.forEach { alt ->
                if (alt.source !in seenSources && !IgnoredUpdates.has(app.packageName, alt.source, alt.versionName)) {
                    if (VersionCompare.isNewer(alt.versionName, app.versionName, app.versionCode, alt.versionCode)) {
                        jobs.add(UpdateAllJob(app.packageName, app.label, alt.source, alt.downloadUrl, alt.versionName))
                    }
                }
            }
            return jobs
        }
        if (app.latestListings.any {
                it.listed && VersionCompare.isNewer(it.versionName, app.versionName, app.versionCode, it.versionCode)
            }
        ) {
            val alt = UpdateArtifactMemory.best(app.packageName)?.takeUnless {
                IgnoredUpdates.has(app.packageName, it.source, it.versionName ?: app.remoteVersionName)
            }
            if (alt != null && VersionCompare.isNewer(alt.versionName, app.versionName, app.versionCode, alt.versionCode)) {
                return listOf(
                    UpdateAllJob(app.packageName, app.label, alt.source, null, alt.versionName ?: app.remoteVersionName),
                )
            }
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
        if (app.origin == AppOrigin.Play && source != RemoteReleasedSource.Play) return emptyList()
        if (!UpdateAll.fetchable(source, app.packageName, auroraPlay)) return emptyList()
        if (IgnoredUpdates.has(app.packageName, source, app.remoteVersionName)) return emptyList()
        return listOf(UpdateAllJob(app.packageName, app.label, source, null, app.remoteVersionName))
    }

    fun groups(
        apps: List<InstalledApp>,
        selected: Set<String> = emptySet(),
        deviceSdk: Int = 0,
        deviceAbis: Set<String> = emptySet(),
        auroraPlay: Boolean = true,
    ): List<List<UpdateAllJob>> {
        val pool = if (selected.isEmpty()) apps else apps.filter { it.packageName in selected }
        return pool.filter(UpdateInventory::hasUpdate)
            .filter { !SignerReplaceQueue.has(it.packageName) }
            .map { candidates(it, deviceSdk, deviceAbis, auroraPlay) }
            .filter { it.isNotEmpty() }
    }

    private fun byNewest(left: UpdateLink, right: UpdateLink): Int {
        val compared = VersionCompare.compare(left.versionName.orEmpty(), right.versionName.orEmpty())
        if (compared != 0) return -compared
        return UpdateArtifactRank.rank(left.source).compareTo(UpdateArtifactRank.rank(right.source))
    }
}
