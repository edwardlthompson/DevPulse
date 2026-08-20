package dev.foss.goldenpath.inventory

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object RefreshLaunch {
    const val EXTRA = "refresh"
    const val EXTRA_APK_MIRROR = "apk_mirror"
    const val EXTRA_APK_PURE = "apk_pure"

    fun requested(intent: Intent?): Boolean =
        intent?.getBooleanExtra(EXTRA, false) == true

    fun flag(intent: Intent?, key: String): Boolean? =
        intent?.takeIf { it.hasExtra(key) }?.getBooleanExtra(key, false)

    fun maybeStart(activity: ComponentActivity, intent: Intent?) {
        val refresh = requested(intent)
        val mirror = flag(intent, EXTRA_APK_MIRROR)
        val pure = flag(intent, EXTRA_APK_PURE)
        val download = DownloadLaunch.packageName(intent)
        val downloadUrl = DownloadLaunch.urlOverride(intent)
        intent?.removeExtra(EXTRA)
        intent?.removeExtra(EXTRA_APK_MIRROR)
        intent?.removeExtra(EXTRA_APK_PURE)
        intent?.removeExtra(DownloadLaunch.EXTRA)
        intent?.removeExtra(DownloadLaunch.EXTRA_URL)
        if (refresh) {
            activity.lifecycleScope.launch {
                val prefs = InventoryPreferences(activity)
                if (mirror != null) prefs.setApkMirrorLookupEnabled(mirror)
                if (pure != null) prefs.setApkPureLookupEnabled(pure)
                val ack = prefs.queryAllPackagesAcknowledged.first()
                if (!QueryAllPackagesGate.canScan(ack, Build.VERSION.SDK_INT)) {
                    Log.i("DevPulse", "refresh launch skipped: query-all-packages not acknowledged")
                    return@launch
                }
                Log.i("DevPulse", "refresh launch start")
                ReleaseRefreshService.start(activity)
            }
        }
        if (download != null) {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                DownloadLaunch.run(activity, download, url = downloadUrl)
            }
        }
    }
}
