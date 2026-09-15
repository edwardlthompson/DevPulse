package dev.foss.goldenpath.inventory

import java.io.File

object UpdatePrefetch {
    fun scoped(
        artifacts: List<UpdateArtifact>,
        wanted: Collection<String>,
    ): List<UpdateArtifact> {
        if (wanted.isEmpty()) return artifacts
        val names = wanted.toSet()
        return artifacts.filter { it.packageName in names }
    }

    fun candidates(
        enabled: Boolean,
        unmetered: Boolean,
        artifacts: List<UpdateArtifact>,
        installed: (String) -> InstalledIdentity?,
        auroraPlay: Boolean = false,
    ): List<UpdateArtifact> {
        if (!enabled || !unmetered) return emptyList()
        return artifacts.mapNotNull { artifact ->
            if (artifact.source == RemoteReleasedSource.Play) {
                if (!auroraPlay) return@mapNotNull null
            } else if (UpdateArtifactRank.rank(artifact.source) >= 99) {
                return@mapNotNull null
            }
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
        fetchFile: (String, File) -> Result<File>,
        inspect: (File) -> ApkInspect,
        installed: (String) -> InstalledIdentity?,
        auroraPlay: Boolean = false,
    ): Int {
        var ready = 0
        candidates(enabled, unmetered, artifacts, installed, auroraPlay).forEach { artifact ->
            val known = installed(artifact.packageName) ?: return@forEach
            val dest = ApkFileStore.fileFor(cacheDir, artifact)
            fetchFile(artifact.downloadUrl, dest).getOrNull() ?: return@forEach
            if (UpdateCache.stageFile(cacheDir, artifact, dest, inspect, known).isSuccess) {
                ready += 1
            }
        }
        return ready
    }
}
