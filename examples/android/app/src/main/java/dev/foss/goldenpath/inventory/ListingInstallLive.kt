package dev.foss.goldenpath.inventory

import android.content.Context
import android.util.Log
import dev.foss.goldenpath.index.apkpure.ApkPureDirect
import dev.foss.goldenpath.index.apkpure.ApkPureHttpFetcher
import dev.foss.goldenpath.index.aurora.AuroraAuth
import dev.foss.goldenpath.index.aurora.AuroraPlayBundle
import dev.foss.goldenpath.index.aurora.AuroraPlayDirect
import dev.foss.goldenpath.index.aurora.AuroraPlayLive
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
        val files = prepare(context, packageName, source, pageUrl, onProgress = onProgress)
        val result = install(context, files, method)
        if (result == OneClickResult.Installed) AppliedUpdates.settle(packageName)
        note(context, startedAt, result)
        return result
    }

    fun install(context: Context, files: List<File>?, method: InstallMethod): OneClickResult {
        if (files.isNullOrEmpty()) return OneClickResult.Failed(ListingFail.why)
        if (!signerOk(context, files.first())) return OneClickResult.Failed(InstallWhy.Signing)
        val used = method.effective(WelcomeNeeds.installGranted(context))
        if (files.size > 1) {
            if (!WelcomeNeeds.ensureInstall(context)) return OneClickResult.Failed(InstallWhy.Permission)
            return runCatching { SessionApkInstall.start(context, files) }
                .fold({ OneClickResult.Installed }, { OneClickResult.Failed(InstallWhy.Permission) })
        }
        return ApkInstall.apply(context, files.first(), used).toClick()
    }

    fun prepare(
        context: Context,
        packageName: String,
        source: RemoteReleasedSource,
        pageUrl: String?,
        onProgress: (Long, Long) -> Unit = { _, _ -> },
        installedVersion: String? = null,
    ): List<File>? {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return ListingFail.none()
        val listed = RemoteReleaseMemory.byPackage[pkg]?.offers?.firstOrNull { it.source == source }?.versionName
        if (!ListingNewer.allow(listed, installedVersion)) return ListingFail.older()
        if (source == RemoteReleasedSource.Play) return play(context, pkg, onProgress)
        val artifact = ListingDirect.resolve(
            packageName = pkg,
            source = source,
            pageUrl = pageUrl,
            fetchPage = ListingPageHttp::get,
            fetchReleases = { repo -> ListingInstallFetch.releases(context, repo) },
            resolveApkPure = { ApkPureDirect.resolve(it, ApkPureHttpFetcher) },
            resolveAptoide = { ListingInstallFetch.aptoide(it) },
            resolvePlay = { AuroraPlayDirect.resolve(it, AuroraPlayLive.files(context)) },
            fdroidCache = { name, src ->
                DownloadLaunch.fromFdroidCache(File(context.filesDir, "fdroid-index"), name, System.currentTimeMillis(), src)
            },
            githubOpt = ListingForgeFiles.opt(context.filesDir, pkg),
            directApkUrl = ListingForgeFiles.apk(context.filesDir, pkg),
        )
        if (artifact == null) {
            Log.i("DevPulse", "listing ${source.name} $pkg no file")
            return ListingFail.none()
        }
        artifact.localPath?.let(::File)?.takeIf { it.isFile }?.let { return listOf(it) }
        val cache = File(context.cacheDir, "updates")
        if (!StorageRoom.enough(cache)) return ListingFail.none()
        val bytes = ApkHttpFetcher.get(artifact.downloadUrl, onProgress).getOrElse {
            Log.i("DevPulse", "listing ${source.name} $pkg download fail ${it.message}")
            return ListingFail.none()
        }
        val file = ListingDownload.write(
            cache,
            artifact,
            bytes,
            inspect = { ApkArchiveIdentity.inspect(context.packageManager, it) },
        )
        if (file == null) {
            Log.i("DevPulse", "listing ${source.name} $pkg package mismatch")
            return ListingFail.signing()
        }
        return listOf(file)
    }

    private fun play(context: Context, pkg: String, onProgress: (Long, Long) -> Unit): List<File>? {
        val parts = AuroraPlayBundle.files(pkg, AuroraPlayLive.files(context))
        if (parts.isEmpty()) {
            Log.i("DevPulse", "listing Play $pkg no file")
            return ListingFail.none()
        }
        Log.i("DevPulse", "listing Play $pkg files=${parts.size}")
        val cache = File(context.cacheDir, "updates")
        if (!StorageRoom.enough(cache)) return ListingFail.none()
        val files = ListingPlay.download(
            cache,
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
        return files ?: ListingFail.signing()
    }

    private fun signerOk(context: Context, file: File): Boolean {
        val apk = ApkArchiveIdentity.inspect(context.packageManager, file)
        val device = apk.packageName?.let { ApkArchiveIdentity.installed(context.packageManager, it) }
        return ApkIdentity.signersMatch(apk.signers, device?.signers)
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
}

