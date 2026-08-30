package dev.foss.goldenpath.inventory

import android.content.Context
import java.io.File

sealed class RollbackResult {
    data object Success : RollbackResult()
    data class Failed(val reason: String) : RollbackResult()
}

fun interface RollbackDownloadFetcher {
    fun toFile(url: String, dest: File, onProgress: (Long, Long) -> Unit): Result<File>
}

object AppRollback {
    fun rollback(
        context: Context,
        app: InstalledApp,
        version: AppVersionItem,
        onProgress: (Long, Long) -> Unit = { _, _ -> },
        fetcher: RollbackDownloadFetcher = RollbackDownloadFetcher { u, d, p -> ApkHttpFetcher.toFile(u, d, p) },
        inspectApk: (File) -> ApkInspect = { ApkArchiveIdentity.inspect(context.packageManager, it) },
    ): RollbackResult {
        val pkg = app.packageName
        val url = version.downloadUrl
        if (url.isNullOrBlank()) {
            return RollbackResult.Failed("No direct download link available for version ${version.versionName}")
        }
        val cache = File(context.cacheDir, "updates")
        if (!cache.isDirectory) cache.mkdirs()
        if (!StorageRoom.enough(cache)) {
            return RollbackResult.Failed("Not enough storage space")
        }
        val dest = File(cache, "${pkg}_rollback_${System.currentTimeMillis()}.apk")
        val written = fetcher.toFile(url, dest, onProgress).getOrElse { err ->
            dest.delete()
            return RollbackResult.Failed(err.message ?: "Download failed")
        }
        val apk = inspectApk(written)
        if (apk.packageName != pkg) {
            written.delete()
            return RollbackResult.Failed("Downloaded APK package mismatch (${apk.packageName ?: "invalid"})")
        }
        val captured = SignerReplaceStore.capture(
            filesDir = context.filesDir,
            packageName = pkg,
            label = app.label,
            source = version.source,
            files = listOf(written),
        )
        written.delete()
        if (!captured) {
            return RollbackResult.Failed("Failed to stage rollback APK")
        }
        RefreshTrace.line("rollback staged $pkg ${version.versionName}")
        return RollbackResult.Success
    }
}
