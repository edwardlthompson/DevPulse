package dev.foss.goldenpath.inventory

import java.io.File

sealed class OneClickKind {
    data class Direct(val artifact: UpdateArtifact) : OneClickKind()
    data class Play(val packageName: String) : OneClickKind()
    data class ApkPure(val packageName: String) : OneClickKind()
    data object None : OneClickKind()
}

sealed class OneClickResult {
    data object Installed : OneClickResult()
    data object PlayOpened : OneClickResult()
    data object ApkPureOpened : OneClickResult()
    data class Failed(val why: InstallWhy) : OneClickResult()
    data object None : OneClickResult()
}

object OneClickUpdate {
    fun kind(packageName: String, listings: List<UpdateLink>): OneClickKind {
        UpdateArtifactMemory.best(packageName)
            ?.takeUnless { IgnoredUpdates.has(packageName, it.source, it.versionName) }
            ?.let { return OneClickKind.Direct(it) }
        val usable = listings.filter { UpdateInventory.canOpen(it, packageName) }
        if (usable.any { it.source == RemoteReleasedSource.Play }) {
            return OneClickKind.Play(packageName)
        }
        if (usable.any { it.source == RemoteReleasedSource.ApkPure }) {
            return OneClickKind.ApkPure(packageName)
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
        resolveApkPure: (String) -> UpdateArtifact? = { null },
        resolveAurora: (String) -> UpdateArtifact? = { null },
        filesDir: File? = null,
    ): OneClickResult {
        val startedAt = System.currentTimeMillis()
        val result = when (kind) {
            is OneClickKind.None -> OneClickResult.None
            is OneClickKind.Play -> {
                val artifact = resolveAurora(kind.packageName)
                if (artifact != null) {
                    applyDirect(artifact, cacheDir, fetch, install, inspect, installed)
                } else {
                    openPlay(kind.packageName)
                    OneClickResult.PlayOpened
                }
            }
            is OneClickKind.ApkPure -> {
                val artifact = resolveApkPure(kind.packageName)
                if (artifact != null) {
                    applyDirect(artifact, cacheDir, fetch, install, inspect, installed)
                } else {
                    RefreshTrace.line("apkpure ${kind.packageName} no asset")
                    OneClickResult.Failed(InstallWhy.NoFile)
                }
            }
            is OneClickKind.Direct -> applyDirect(kind.artifact, cacheDir, fetch, install, inspect, installed)
        }
        if (result is OneClickResult.Failed) {
            rememberFail(kind, filesDir)
        }
        filesDir?.let {
            PulseHistory.note(
                it,
                "update",
                System.currentTimeMillis() - startedAt,
                if (result == OneClickResult.Installed) 1 else 0,
                "result=${result::class.simpleName}",
            )
        }
        return result
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
        if (cached == null && !StorageRoom.enough(cacheDir)) {
            return OneClickResult.Failed(InstallWhy.NoFile)
        }
        val file = cached ?: run {
            val bytes = fetch.get(artifact.downloadUrl).getOrElse { return OneClickResult.Failed(InstallWhy.NoFile) }
            UpdateCache.stage(cacheDir, artifact, bytes, inspect, installed).getOrElse {
                val why = if (it.message == "identity") InstallWhy.Signing else InstallWhy.NoFile
                return OneClickResult.Failed(why)
            }
        }
        if (!ApkIdentity.signersMatch(inspect(file).signers, installed.signers)) {
            return OneClickResult.Failed(InstallWhy.Signing)
        }
        return install(file).toClick()
    }

    private fun rememberFail(kind: OneClickKind, filesDir: File?) {
        when (kind) {
            is OneClickKind.Direct ->
                IgnoredUpdates.add(kind.artifact.packageName, kind.artifact.source, kind.artifact.versionName, filesDir)
            is OneClickKind.Play ->
                IgnoredUpdates.add(kind.packageName, RemoteReleasedSource.Play, null, filesDir)
            is OneClickKind.ApkPure ->
                IgnoredUpdates.add(kind.packageName, RemoteReleasedSource.ApkPure, null, filesDir)
            is OneClickKind.None -> Unit
        }
    }
}
