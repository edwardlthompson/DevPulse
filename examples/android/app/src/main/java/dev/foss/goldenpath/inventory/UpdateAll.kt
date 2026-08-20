package dev.foss.goldenpath.inventory

import java.io.File

data class UpdateAllResult(
    val downloaded: Int,
    val installed: Int,
    val failedDownload: Int,
    val failedInstall: Int,
)

object UpdateAll {
    const val MAX_FILES = 40

    fun artifacts(apps: List<InstalledApp>): List<UpdateArtifact> =
        apps.filter(UpdateInventory::hasUpdate).mapNotNull { app ->
            (OneClickUpdate.kind(app.packageName, app.latestListings) as? OneClickKind.Direct)?.artifact
        }

    fun run(
        artifacts: List<UpdateArtifact>,
        cacheDir: File,
        fetch: ApkBytesFetcher,
        install: (File) -> ApkInstallResult,
        inspect: (File) -> ApkInspect,
        installedOf: (String) -> InstalledIdentity,
        maxFiles: Int = MAX_FILES,
    ): UpdateAllResult {
        val files = mutableListOf<File>()
        var failedDownload = 0
        artifacts.forEach { artifact ->
            val cached = artifact.localPath?.let(::File)?.takeIf { it.isFile }
            if (cached != null) {
                files += cached
                return@forEach
            }
            val bytes = fetch.get(artifact.downloadUrl).getOrNull()
            val staged = bytes?.let {
                UpdateCache.stage(cacheDir, artifact, it, inspect, installedOf(artifact.packageName), maxFiles)
            }?.getOrNull()
            if (staged == null) failedDownload += 1 else files += staged
        }
        var installed = 0
        var failedInstall = 0
        files.forEach { file ->
            when (install(file)) {
                is ApkInstallResult.Failed -> failedInstall += 1
                else -> installed += 1
            }
        }
        return UpdateAllResult(files.size, installed, failedDownload, failedInstall)
    }
}
