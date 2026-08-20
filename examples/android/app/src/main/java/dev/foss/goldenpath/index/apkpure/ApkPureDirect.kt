package dev.foss.goldenpath.index.apkpure

import dev.foss.goldenpath.inventory.UpdateArtifact
import dev.foss.goldenpath.inventory.UpdateArtifactMemory

/** On-demand APKPure `asset.url`, same get_app_update path APKUpdater uses. */
object ApkPureDirect {
    fun resolve(packageName: String, fetch: ApkPureBatchFetcher): UpdateArtifact? {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return null
        UpdateArtifactMemory.best(pkg)?.let { return it }
        val json = fetch.fetch(listOf(pkg)).getOrNull() ?: return null
        ApkPureMetaParser.parseMany(json)
        return UpdateArtifactMemory.best(pkg)
    }
}
