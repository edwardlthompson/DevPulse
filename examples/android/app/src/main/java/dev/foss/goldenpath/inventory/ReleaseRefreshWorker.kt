package dev.foss.goldenpath.inventory

import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dev.foss.goldenpath.network.NetworkUnmetered
import dev.foss.goldenpath.notify.RefreshNotifier
import dev.foss.goldenpath.notify.RefreshNotifyCopy
import dev.foss.goldenpath.notify.UpdatesNotify
import kotlinx.coroutines.flow.first

class ReleaseRefreshWorker(
    context: android.content.Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val notifier = RefreshNotifier(applicationContext)

    override suspend fun getForegroundInfo(): ForegroundInfo {
        notifier.ensureChannel()
        val type = if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0
        return ForegroundInfo(RefreshNotifyCopy.PROGRESS_ID, notifier.progress(0, 0), type)
    }

    override suspend fun doWork(): Result {
        val prefs = InventoryPreferences(applicationContext)
        val canScan = QueryAllPackagesGate.canScan(
            prefs.queryAllPackagesAcknowledged.first(),
            Build.VERSION.SDK_INT,
        )
        if (!canScan) return Result.success()
        val wifiOnly = RefreshWifiPrefs(applicationContext).enabled.first()
        if (!RefreshWifiOnly.allow(wifiOnly, NetworkUnmetered.isUnmetered(applicationContext))) {
            return Result.success()
        }
        if (!ReleaseRefreshRuntime.tryBegin()) return Result.success()
        notifier.ensureChannel()
        setForeground(getForegroundInfo())
        var lookedUp = 0
        try {
            lookedUp = ReleaseRefreshRunner.run(
                applicationContext,
                { progress ->
                    ReleaseRefreshRuntime.setProgress(progress)
                    notifier.postProgress(progress.done, progress.total, progress.location)
                    lookedUp = RefreshNotifyCopy.lookedUpCount(progress)
                },
            )
        } catch (_: Throwable) {
            lookedUp = 0
        } finally {
            runCatching {
                notifier.postDone(lookedUp)
                ReleaseRefreshRuntime.finish()
            }
        }
        val apps = PackageManagerPackageCatalog(applicationContext.packageManager)
            .listInstalled()
            .map(RemoteReleaseMemory::merge)
        UpdatesNotify.post(applicationContext, apps)
        return Result.success()
    }
}
