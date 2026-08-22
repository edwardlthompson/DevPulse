package dev.foss.goldenpath.inventory

import android.content.Context
import android.util.Log
import dev.foss.goldenpath.index.apkpure.ApkPureDirect
import dev.foss.goldenpath.index.apkpure.ApkPureHttpFetcher
import dev.foss.goldenpath.index.aptoide.AptoideHttpFetcher
import dev.foss.goldenpath.index.aptoide.AptoideScan
import dev.foss.goldenpath.index.aurora.AuroraAuth
import dev.foss.goldenpath.index.aurora.AuroraPlayBundle
import dev.foss.goldenpath.index.aurora.AuroraPlayDirect
import dev.foss.goldenpath.index.aurora.AuroraPlayLive
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.index.forge.GitHubSearchHttp
import java.io.File

object ListingInstallLive {
    fun run(
        context: Context,
        packageName: String,
        source: RemoteReleasedSource,
        pageUrl: String?,
        method: InstallMethod,
        onProgress: (Long, Long) -> Unit = { _, _ -> },
    ): OneClickResult {
        val startedAt = System.currentTimeMillis()
        val files = prepare(context, packageName, source, pageUrl, onProgress)
        val result = install(context, files, method)
        if (result == OneClickResult.Installed) AppliedUpdates.settle(packageName)
        note(context, startedAt, result)
        return result
    }

    fun install(context: Context, files: List<File>?, method: InstallMethod): OneClickResult {
        if (!WelcomeNeeds.ensureInstall(context)) return OneClickResult.FailedInstall
        if (files.isNullOrEmpty()) return OneClickResult.FailedDownload
        if (files.size > 1) {
            return runCatching { SessionApkInstall.start(context, files) }
                .fold({ OneClickResult.Installed }, { OneClickResult.FailedInstall })
        }
        return when (ApkInstall.apply(context, files.first(), method)) {
            is ApkInstallResult.Failed -> OneClickResult.FailedInstall
            else -> OneClickResult.Installed
        }
    }

    fun prepare(
        context: Context,
        packageName: String,
        source: RemoteReleasedSource,
        pageUrl: String?,
        onProgress: (Long, Long) -> Unit = { _, _ -> },
    ): List<File>? {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return null
        if (source == RemoteReleasedSource.Play) return play(context, pkg, onProgress)
        val artifact = ListingDirect.resolve(
            packageName = pkg,
            source = source,
            pageUrl = pageUrl,
            fetchPage = ListingPageHttp::get,
            fetchReleases = { repo -> releases(context, repo) },
            resolveApkPure = { ApkPureDirect.resolve(it, ApkPureHttpFetcher) },
            resolveAptoide = { aptoide(it) },
            resolvePlay = { AuroraPlayDirect.resolve(it, AuroraPlayLive.files(context)) },
            fdroidCache = { name, src ->
                DownloadLaunch.fromFdroidCache(File(context.filesDir, "fdroid-index"), name, System.currentTimeMillis(), src)
            },
        )
        if (artifact == null) {
            Log.i("DevPulse", "listing ${source.name} $pkg no file")
            return null
        }
        artifact.localPath?.let(::File)?.takeIf { it.isFile }?.let { return listOf(it) }
        val bytes = ApkHttpFetcher.get(artifact.downloadUrl, onProgress).getOrElse {
            Log.i("DevPulse", "listing ${source.name} $pkg download fail ${it.message}")
            return null
        }
        val file = ListingDownload.write(
            File(context.cacheDir, "updates"),
            artifact,
            bytes,
            inspect = { ApkArchiveIdentity.inspect(context.packageManager, it) },
        )
        if (file == null) Log.i("DevPulse", "listing ${source.name} $pkg package mismatch")
        return file?.let { listOf(it) }
    }

    private fun play(context: Context, pkg: String, onProgress: (Long, Long) -> Unit): List<File>? {
        val parts = AuroraPlayBundle.files(pkg, AuroraPlayLive.files(context))
        if (parts.isEmpty()) {
            Log.i("DevPulse", "listing Play $pkg no file")
            return null
        }
        Log.i("DevPulse", "listing Play $pkg files=${parts.size}")
        val files = ListingPlay.download(
            File(context.cacheDir, "updates"),
            pkg,
            parts,
            fetch = { url, progress ->
                ApkHttpFetcher.get(url, progress, AuroraAuth.USER_AGENT).onFailure {
                    Log.i("DevPulse", "listing Play $pkg part fail ${it.message}")
                }
            },
            inspect = { ApkArchiveIdentity.inspect(context.packageManager, it) },
            onProgress = onProgress,
        )
        Log.i("DevPulse", "listing Play $pkg kept=${files?.size ?: 0}")
        return files
    }

    private fun note(context: Context, startedAt: Long, result: OneClickResult) {
        PulseHistory.note(
            context.filesDir,
            "update",
            System.currentTimeMillis() - startedAt,
            if (result == OneClickResult.Installed) 1 else 0,
            "result=${result::class.simpleName}",
        )
    }

    private fun aptoide(packageName: String): UpdateArtifact? {
        AptoideScan.toPick(
            AptoideScan.lookupOne(packageName, AptoideHttpFetcher, System.currentTimeMillis(), force = true),
            packageName,
        )
        return UpdateArtifactMemory.forSource(packageName, RemoteReleasedSource.Aptoide)
    }

    private fun releases(context: Context, ownerRepo: String): String? {
        val page = GitHubSearchHttp(EncryptedForgeTokenStore.wrap(context).getToken()).listReleases(ownerRepo)
        return page.body.takeIf { page.statusCode in 200..299 }
    }
}

