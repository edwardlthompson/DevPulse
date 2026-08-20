package dev.foss.goldenpath.inventory

import java.io.File

object UpdatePrefetch {
    fun candidates(
        enabled: Boolean,
        unmetered: Boolean,
        artifacts: List<UpdateArtifact>,
        installed: (String) -> InstalledIdentity?,
    ): List<UpdateArtifact> {
        if (!enabled || !unmetered) return emptyList()
        return artifacts.mapNotNull { artifact ->
            if (UpdateArtifactRank.rank(artifact.source) >= 99) return@mapNotNull null
            if (artifact.localPath != null) return@mapNotNull null
            val known = installed(artifact.packageName) ?: return@mapNotNull null
            if (artifact.nativeCodes.isNotEmpty() && known.abis.isNotEmpty() &&
                artifact.nativeCodes.intersect(known.abis).isEmpty()
            ) {
                return@mapNotNull null
            }
            artifact
        }
    }

    fun run(
        enabled: Boolean,
        unmetered: Boolean,
        cacheDir: File,
        artifacts: List<UpdateArtifact>,
        fetch: ApkBytesFetcher,
        inspect: (File) -> ApkInspect,
        installed: (String) -> InstalledIdentity?,
    ): Int {
        var ready = 0
        candidates(enabled, unmetered, artifacts, installed).forEach { artifact ->
            val known = installed(artifact.packageName) ?: return@forEach
            val bytes = fetch.get(artifact.downloadUrl).getOrNull() ?: return@forEach
            if (UpdateCache.stage(cacheDir, artifact, bytes, inspect, known).isSuccess) {
                ready += 1
            }
        }
        return ready
    }
}
