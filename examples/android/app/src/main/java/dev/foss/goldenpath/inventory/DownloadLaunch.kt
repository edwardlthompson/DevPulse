package dev.foss.goldenpath.inventory

import android.content.Context
import android.util.Log
import dev.foss.goldenpath.index.fdroid.FdroidApkUrl
import dev.foss.goldenpath.index.fdroid.FdroidIndexParser
import dev.foss.goldenpath.index.fdroid.FdroidIndexStore
import dev.foss.goldenpath.index.fdroid.FdroidRepoCatalog
import java.io.File

object DownloadLaunch {
    const val EXTRA = "download_package"
    const val EXTRA_URL = "download_url"
    private val cacheRepos = FdroidRepoCatalog.defaults().map { it.id }

    fun packageName(intent: android.content.Intent?): String? =
        intent?.getStringExtra(EXTRA)?.trim()?.ifEmpty { null }

    fun reason(artifact: UpdateArtifact?): String = when {
        artifact == null -> "no file url"
        UpdateArtifactRank.rank(artifact.source) >= 99 -> "page only"
        else -> "direct"
    }

    fun fromFdroidCache(dir: File, packageName: String, nowMs: Long): UpdateArtifact? {
        val store = FdroidIndexStore(dir)
        cacheRepos.forEach { repoId ->
            val data = File(dir, "$repoId.index")
            if (!data.isFile) return@forEach
            val raw = store.load(repoId, nowMs) ?: return@forEach
            val rec = FdroidIndexParser.parse(raw, repoId, setOf(packageName)).firstOrNull() ?: return@forEach
            val url = FdroidApkUrl.of(repoId, rec.apkName) ?: return@forEach
            return UpdateArtifact(
                packageName,
                ListingChannels.sourceForRepo(repoId),
                url,
                rec.suggestedVersionName,
                sha256 = rec.apkSha256,
                nativeCodes = rec.nativeCodes,
            )
        }
        return null
    }

    fun urlOverride(intent: android.content.Intent?): String? =
        intent?.getStringExtra(EXTRA_URL)?.trim()?.ifEmpty { null }

    fun run(
        context: Context,
        packageName: String,
        fetch: ApkBytesFetcher = ApkHttpFetcher,
        url: String? = null,
    ): String {
        val artifact = when {
            !url.isNullOrBlank() -> UpdateArtifact(packageName, RemoteReleasedSource.Fdroid, url)
            else -> UpdateArtifactMemory.best(packageName)
                ?: fromFdroidCache(File(context.filesDir, "fdroid-index"), packageName, System.currentTimeMillis())
        }
        val why = reason(artifact)
        if (artifact == null || why != "direct") {
            Log.i("DevPulse", "download skipped: $why $packageName")
            return why
        }
        val bytes = fetch.get(artifact.downloadUrl).getOrElse {
            Log.i("DevPulse", "download failed $packageName")
            return "failed"
        }
        val staged = UpdateCache.stage(
            File(context.cacheDir, "updates"),
            artifact,
            bytes,
            inspect = { file -> ApkArchiveIdentity.inspect(context.packageManager, file) },
            installed = ApkArchiveIdentity.installed(context.packageManager, packageName)
                ?: InstalledIdentity(packageName, emptySet()),
        )
        return if (staged.isSuccess) {
            Log.i("DevPulse", "download ready $packageName ${staged.getOrNull()?.name}")
            "ready"
        } else {
            Log.i("DevPulse", "download identity failed $packageName")
            "identity"
        }
    }
}
