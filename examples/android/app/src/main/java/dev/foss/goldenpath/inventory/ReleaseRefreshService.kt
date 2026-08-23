package dev.foss.goldenpath.inventory

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import dev.foss.goldenpath.network.NetworkUnmetered
import dev.foss.goldenpath.notify.RefreshNotifier
import dev.foss.goldenpath.notify.RefreshNotifyCopy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReleaseRefreshService : Service() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var notifier: RefreshNotifier

    override fun onCreate() {
        super.onCreate()
        notifier = RefreshNotifier(applicationContext)
        notifier.ensureChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceCompat.startForeground(
            this,
            RefreshNotifyCopy.PROGRESS_ID,
            notifier.progress(0, 0),
            if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0,
        )
        if (!ReleaseRefreshRuntime.tryBegin()) return START_NOT_STICKY
        val wanted = intent?.getStringArrayListExtra(RefreshScope.EXTRA_PACKAGES).orEmpty()
        scope.launch { runRefresh(wanted) }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        job.cancel()
        ReleaseRefreshRuntime.finish()
        super.onDestroy()
    }

    private suspend fun runRefresh(wanted: Collection<String>) {
        var lookedUp = 0
        try {
            lookedUp = ReleaseRefreshRunner.run(
                applicationContext,
                { progress ->
                    ReleaseRefreshRuntime.setProgress(progress)
                    notifier.postProgress(progress.done, progress.total, progress.location)
                    lookedUp = RefreshNotifyCopy.lookedUpCount(progress)
                },
                wanted,
            )
        } catch (_: Throwable) {
            lookedUp = 0
        } finally {
            runCatching {
                notifier.postDone(lookedUp)
                ReleaseRefreshRuntime.finish()
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    companion object {
        fun start(context: Context, packages: Collection<String> = emptySet()) {
            if (ReleaseRefreshRuntime.running.value) return
            val wifiOnly = RefreshWifiPrefs(context).blockingEnabled()
            if (!RefreshWifiOnly.allow(wifiOnly, NetworkUnmetered.isUnmetered(context))) {
                Log.i("DevPulse", "refresh skipped: wifi only")
                return
            }
            val intent = Intent(context.applicationContext, ReleaseRefreshService::class.java)
            val names = RefreshScope.names(packages)
            if (names.isNotEmpty()) intent.putStringArrayListExtra(RefreshScope.EXTRA_PACKAGES, names)
            ContextCompat.startForegroundService(context.applicationContext, intent)
        }
    }
}
