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

    fun requested(intent: Intent?): Boolean =
        intent?.getBooleanExtra(EXTRA, false) == true

    fun maybeStart(activity: ComponentActivity, intent: Intent?) {
        if (!requested(intent)) return
        intent?.removeExtra(EXTRA)
        activity.lifecycleScope.launch {
            val ack = InventoryPreferences(activity).queryAllPackagesAcknowledged.first()
            if (!QueryAllPackagesGate.canScan(ack, Build.VERSION.SDK_INT)) {
                Log.i("DevPulse", "refresh launch skipped: query-all-packages not acknowledged")
                return@launch
            }
            Log.i("DevPulse", "refresh launch start")
            ReleaseRefreshService.start(activity)
        }
    }
}
