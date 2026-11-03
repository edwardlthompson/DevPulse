package dev.foss.goldenpath.inventory

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RefreshCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_CANCEL) return
        RefreshSkip.stopAll()
        ReleaseRefreshRuntime.pause()
        ReleaseRefreshRuntime.finish()
        context.stopService(Intent(context, ReleaseRefreshService::class.java))
    }

    companion object {
        const val ACTION_CANCEL = "dev.foss.goldenpath.REFRESH_CANCEL"
    }
}
