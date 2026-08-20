package dev.foss.goldenpath.inventory

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
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
        scope.launch { runRefresh() }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        job.cancel()
        ReleaseRefreshRuntime.finish()
        super.onDestroy()
    }

    private suspend fun runRefresh() {
        var lookedUp = 0
        try {
            lookedUp = ReleaseRefreshRunner.run(applicationContext) { progress ->
                ReleaseRefreshRuntime.setProgress(progress)
                notifier.postProgress(progress.done, progress.total, progress.location)
                lookedUp = RefreshNotifyCopy.lookedUpCount(progress)
            }
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
        fun start(context: Context) {
            if (ReleaseRefreshRuntime.running.value) return
            ContextCompat.startForegroundService(
                context.applicationContext,
                Intent(context.applicationContext, ReleaseRefreshService::class.java),
            )
        }
    }
}
