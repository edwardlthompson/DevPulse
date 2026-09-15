package dev.foss.goldenpath.inventory

import android.content.Context
import android.os.Build
import android.util.Log
import dev.foss.goldenpath.index.aurora.AuroraAuth
import dev.foss.goldenpath.index.aurora.AuroraPlayBundle
import dev.foss.goldenpath.index.aurora.AuroraPlayLive
import java.io.File

object ListingPlayInstall {
    fun files(
        context: Context,
        pkg: String,
        have: String?,
        haveCode: Long,
        onProgress: (Long, Long) -> Unit,
    ): List<File>? {
        val parts = AuroraPlayBundle.files(pkg, AuroraPlayLive.files(context))
        if (parts.isEmpty()) {
            Log.i("DevPulse", "listing Play $pkg no file")
            return if (AuroraPlayLive.why(pkg) == InstallWhy.PlayPurchase) ListingFail.playPurchase() else ListingFail.none()
        }
        if (!ListingPlay.newerThanInstalled(parts, have, haveCode)) {
            Log.i("DevPulse", "listing Play $pkg older listed=${parts.firstOrNull()?.versionName} installed=$have")
            return ListingFail.older()
        }
        val cache = File(context.cacheDir, "updates")
        if (!StorageRoom.enough(cache)) {
            Log.i("DevPulse", "listing Play $pkg no space ${StorageRoom.bytes(cache)}")
            return ListingFail.space()
        }
        val files = ListingPlay.download(
            cache, pkg, parts,
            save = { url, dest, progress ->
                ApkHttpFetcher.toFile(url, dest, progress, AuroraAuth.USER_AGENT)
                    .onFailure {
                        Log.i("DevPulse", "listing Play $pkg part fail ${it.message}")
                        if (ApkHttpFetcher.retryable(it.message)) ListingFail.timeout()
                    }.isSuccess
            },
            inspect = { ApkArchiveIdentity.inspect(context.packageManager, it, signing = false) },
            onProgress = onProgress,
        )
        return keep(context, files ?: return if (ListingFail.why == InstallWhy.Timeout) null else ListingFail.none(), haveCode)
    }

    fun keep(context: Context, files: List<File>, haveCode: Long): List<File>? =
        ListingApkGuard.keep(
            files, haveCode,
            Build.SUPPORTED_ABIS.filter { it.isNotBlank() }.toSet(),
            { ApkArchiveIdentity.archiveCode(context.packageManager, it) },
        )
}
