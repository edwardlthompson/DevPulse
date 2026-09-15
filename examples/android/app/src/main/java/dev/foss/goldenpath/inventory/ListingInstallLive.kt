package dev.foss.goldenpath.inventory

import android.content.Context
import android.os.Build
import android.util.Log
import dev.foss.goldenpath.index.apkpure.ApkPureDirect
import dev.foss.goldenpath.index.apkpure.ApkPureHttpFetcher
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
        if (files == null && source == RemoteReleasedSource.Play &&
            ListingFail.why != InstallWhy.NoSpace && ListingFail.why != InstallWhy.Older &&
            ListingFail.why != InstallWhy.Sdk && ListingFail.why != InstallWhy.Timeout
        ) {
            PlayStoreIntent.open(context, packageName)
        }
        val result = install(context, files, method)
        if (result == OneClickResult.Installed) {
            val ver = RemoteReleaseMemory.byPackage[packageName]?.offers?.firstOrNull { it.source == source }?.versionName
            AppliedUpdates.settle(packageName, ver, filesDir = context.filesDir)
        }
        note(context, startedAt, result)
        return result
    }

    fun install(context: Context, files: List<File>?, method: InstallMethod): OneClickResult {
        if (files.isNullOrEmpty()) return OneClickResult.Failed(ListingFail.why)
        if (!signerOk(context, files.first())) return OneClickResult.Failed(InstallWhy.Signing)
        val used = method.onDevice(WelcomeNeeds.installGranted(context))
        if (used == InstallMethod.Root) {
            return if (RootSessionInstall.run(files)) OneClickResult.Installed else OneClickResult.Failed(InstallWhy.Permission)
        }
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
        installedCode: Long = 0,
    ): List<File>? {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return ListingFail.none()
        val offer = RemoteReleaseMemory.byPackage[pkg]?.offers?.firstOrNull { it.source == source }
        val have = installedVersion?.trim()?.ifEmpty { null }
            ?: ApkArchiveIdentity.versionName(context.packageManager, pkg)
        val haveCode = if (installedCode > 0L) installedCode else ApkArchiveIdentity.versionCode(context.packageManager, pkg)
        if (!ListingNewer.allow(offer?.versionName, have, haveCode, offer?.versionCode)) {
            Log.i("DevPulse", "listing ${source.name} $pkg older listed=${offer?.versionName} installed=$have")
            return ListingFail.older()
        }
        if (source == RemoteReleasedSource.Play) {
            val files = ListingPlayInstall.files(context, pkg, have, haveCode, onProgress)
            if (!files.isNullOrEmpty()) return files
            if (ListingFail.why == InstallWhy.Older || ListingFail.why == InstallWhy.Sdk) return null
            if (!playSideload(AuroraPlayLive.playHeadersBroken(), AuroraPlayLive.why(pkg))) {
                return if (AuroraPlayLive.why(pkg) == InstallWhy.PlayPurchase) ListingFail.playPurchase() else ListingFail.none()
            }
            val fallback = UpdateArtifactMemory.forSource(pkg, RemoteReleasedSource.ApkPure)
                ?: UpdateArtifactMemory.best(pkg)?.takeUnless { it.source == RemoteReleasedSource.Play }
            if (fallback != null && fallback.downloadUrl.isNotBlank()) {
                Log.i("DevPulse", "listing Play fallback to ${fallback.source.name} $pkg")
                val fallbackFiles = prepare(context, pkg, fallback.source, fallback.downloadUrl, onProgress, installedVersion, installedCode)
                if (!fallbackFiles.isNullOrEmpty()) return fallbackFiles
            }
            return if (AuroraPlayLive.why(pkg) == InstallWhy.PlayPurchase) ListingFail.playPurchase() else ListingFail.none()
        }
        val artifact = ListingDirect.resolve(
            packageName = pkg, source = source, pageUrl = pageUrl, fetchPage = ListingPageHttp::get,
            fetchReleases = { repo -> ListingInstallFetch.releases(context, repo) },
            resolveApkPure = { ApkPureDirect.resolve(it, ApkPureHttpFetcher) },
            resolveAptoide = { ListingInstallFetch.aptoide(context, it) },
            resolvePlay = { AuroraPlayDirect.resolve(it, AuroraPlayLive.files(context)) },
            fdroidCache = { name, src -> DownloadLaunch.fromFdroidCache(File(context.filesDir, "fdroid-index"), name, System.currentTimeMillis(), src) },
            githubOpt = ListingForgeFiles.opt(context.filesDir, pkg), directApkUrl = ListingForgeFiles.apk(context.filesDir, pkg),
        ) ?: run {
            Log.i("DevPulse", "listing ${source.name} $pkg no file")
            return ListingFail.resolveMiss()
        }
        artifact.localPath?.let(::File)?.takeIf { it.isFile }?.let { return ListingPlayInstall.keep(context, listOf(it), haveCode) }
        val deviceAbis = Build.SUPPORTED_ABIS.filter { it.isNotBlank() }.toSet()
        if (!ListingFit.abiOk(artifact.nativeCodes, deviceAbis)) {
            Log.i("DevPulse", "listing ${source.name} $pkg abi ${artifact.nativeCodes.joinToString()}")
            return ListingFail.sdk()
        }
        val cache = File(context.cacheDir, "updates")
        if (!StorageRoom.enough(cache)) {
            Log.i("DevPulse", "listing ${source.name} $pkg no space ${StorageRoom.bytes(cache)}")
            return ListingFail.space()
        }
        val dest = ApkFileStore.fileFor(cache, artifact)
        ApkZip.dropIfInvalid(dest)
        val written = ApkHttpFetcher.toFile(artifact.downloadUrl, dest, onProgress).getOrElse {
            Log.i("DevPulse", "listing ${source.name} $pkg download fail ${it.message}")
            dest.delete()
            return if (ApkHttpFetcher.retryable(it.message)) ListingFail.timeout() else ListingFail.none()
        }
        val inspect = { file: File ->
            if (ApkZip.dropIfInvalid(file)) ApkInspect(null, emptySet())
            else ApkArchiveIdentity.inspect(context.packageManager, file, signing = false)
        }
        val files = ListingDownload.files(written, artifact, inspect, cache)
            ?: run {
                Log.i("DevPulse", "listing ${source.name} $pkg package mismatch")
                return ListingFail.none()
            }
        val libs = files.flatMap { ApkNativeZip.abis(it) }.toSet()
        if (!ListingFit.abiOk(libs, deviceAbis)) {
            if (libs.isNotEmpty()) UpdateArtifactMemory.add(artifact.copy(nativeCodes = libs))
            files.forEach { it.delete() }
            Log.i("DevPulse", "listing ${source.name} $pkg abi ${libs.joinToString()}")
            return ListingFail.sdk()
        }
        return ListingPlayInstall.keep(context, files, haveCode)
    }

    internal fun playSideload(headersBroken: Boolean, why: InstallWhy): Boolean =
        !headersBroken && why != InstallWhy.PlayPurchase

    private fun signerOk(context: Context, file: File): Boolean {
        val apk = ApkArchiveIdentity.inspect(context.packageManager, file, signing = true)
        return ApkIdentity.signersMatch(apk.signers, apk.packageName?.let { ApkArchiveIdentity.installed(context.packageManager, it) }?.signers)
    }

    private fun note(context: Context, startedAt: Long, result: OneClickResult) {
        PulseHistory.note(context.filesDir, "update", System.currentTimeMillis() - startedAt, if (result == OneClickResult.Installed) 1 else 0, "result=${result::class.simpleName}")
    }
}
