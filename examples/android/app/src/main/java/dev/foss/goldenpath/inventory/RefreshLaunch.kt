package dev.foss.goldenpath.inventory

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
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
        if (!requested(intent)) return
        val mirror = flag(intent, EXTRA_APK_MIRROR)
        val pure = flag(intent, EXTRA_APK_PURE)
        intent?.removeExtra(EXTRA)
        intent?.removeExtra(EXTRA_APK_MIRROR)
        intent?.removeExtra(EXTRA_APK_PURE)
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
}
