package dev.foss.goldenpath.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.foss.goldenpath.inventory.PackageManagerPackageCatalog
import dev.foss.goldenpath.inventory.RemoteReleaseMemory
import dev.foss.goldenpath.inventory.StalenessDays
import dev.foss.goldenpath.query.ScanHistoryStore
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

class StaleNotifyWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!StaleNotifyPrefs(applicationContext).enabled.first()) return Result.success()
        val now = System.currentTimeMillis()
        val apps = PackageManagerPackageCatalog(applicationContext.packageManager)
            .listInstalled()
            .map(RemoteReleaseMemory::merge)
        val after = StalenessDays.of(apps, now)
        val store = ScanHistoryStore(File(applicationContext.filesDir, "scan-history"))
        val before = StalenessDays.parse(store.loadDays())
        val hits = after.mapNotNull { (pkg, days) ->
            val next = days ?: return@mapNotNull null
            NotifyPolicy.crossings(before[pkg], next, pkg)
        }
        val grouped = listOf(
            NotifyPolicy.SIX_MONTHS to hits.filter { it.days in NotifyPolicy.SIX_MONTHS until NotifyPolicy.ONE_YEAR }
                .map { it.packageName },
            NotifyPolicy.ONE_YEAR to hits.filter { it.days >= NotifyPolicy.ONE_YEAR }.map { it.packageName },
        ).filter { it.second.isNotEmpty() }
        if (grouped.isNotEmpty()) StaleNotifier(applicationContext).postCrossings(grouped)
        store.saveDays(StalenessDays.encode(after))
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "devpulse_stale_crossing"

        fun apply(context: Context, enabled: Boolean) {
            val wm = WorkManager.getInstance(context.applicationContext)
            if (!enabled) {
                wm.cancelUniqueWork(WORK_NAME)
                return
            }
            val request = PeriodicWorkRequestBuilder<StaleNotifyWorker>(7, TimeUnit.DAYS).build()
            wm.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }
    }
}
