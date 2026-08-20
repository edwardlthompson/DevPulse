package dev.foss.goldenpath.inventory

import java.io.File

sealed class OneClickKind {
    data class Direct(val artifact: UpdateArtifact) : OneClickKind()
    data class Play(val packageName: String) : OneClickKind()
    data object None : OneClickKind()
}

sealed class OneClickResult {
    data object Installed : OneClickResult()
    data object PlayOpened : OneClickResult()
    data object FailedDownload : OneClickResult()
    data object FailedInstall : OneClickResult()
    data object None : OneClickResult()
}

object OneClickUpdate {
    fun kind(packageName: String, listings: List<UpdateLink>): OneClickKind {
        UpdateArtifactMemory.best(packageName)?.let { return OneClickKind.Direct(it) }
        if (listings.any { it.source == RemoteReleasedSource.Play && it.listed }) {
            return OneClickKind.Play(packageName)
        }
        return OneClickKind.None
    }

    fun apply(
        kind: OneClickKind,
        cacheDir: File,
        fetch: ApkBytesFetcher,
        install: (File) -> ApkInstallResult,
        openPlay: (String) -> Unit,
        inspect: (File) -> ApkInspect,
        installed: InstalledIdentity,
    ): OneClickResult = when (kind) {
        is OneClickKind.None -> OneClickResult.None
        is OneClickKind.Play -> {
            openPlay(kind.packageName)
            OneClickResult.PlayOpened
        }
        is OneClickKind.Direct -> applyDirect(kind.artifact, cacheDir, fetch, install, inspect, installed)
    }

    private fun applyDirect(
        artifact: UpdateArtifact,
        cacheDir: File,
        fetch: ApkBytesFetcher,
        install: (File) -> ApkInstallResult,
        inspect: (File) -> ApkInspect,
        installed: InstalledIdentity,
    ): OneClickResult {
        val cached = artifact.localPath?.let(::File)?.takeIf { it.isFile }
        val file = cached ?: run {
            val bytes = fetch.get(artifact.downloadUrl).getOrElse { return OneClickResult.FailedDownload }
            UpdateCache.stage(cacheDir, artifact, bytes, inspect, installed).getOrElse {
                return OneClickResult.FailedDownload
            }
        }
        return when (install(file)) {
            is ApkInstallResult.Failed -> OneClickResult.FailedInstall
            else -> OneClickResult.Installed
        }
    }
}
