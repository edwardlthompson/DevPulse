package dev.foss.goldenpath.about

import android.content.Context
import androidx.activity.ComponentActivity
import java.io.File

object UpdateApplyCoordinator {
    suspend fun applySideloadUpdate(
        activity: ComponentActivity,
        preferences: AppUpdatePreferences,
        asset: ReleaseAsset,
    ): Boolean {
        val context: Context = activity.applicationContext
        val updatesDir = File(context.cacheDir, "updates")
        val apkFile = File(updatesDir, "update.${asset.format}")
        if (!ApkDownloadHelper.download(asset.url, apkFile)) return false
        UpdateApplier.launchApkInstall(context, apkFile)
        preferences.setPendingRestart(true)
        activity.recreate()
        return true
    }
}
