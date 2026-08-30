package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dev.foss.goldenpath.about.CheckSchedule
import java.util.concurrent.TimeUnit

enum class ScanInterval {
    OnDemand,
    Daily,
    Weekly,
    Monthly,
}

object ScanSchedule {
    const val WORK_NAME = "devpulse_release_refresh"

    fun checkKey(interval: ScanInterval): String = when (interval) {
        ScanInterval.OnDemand -> "off"
        ScanInterval.Daily -> "daily"
        ScanInterval.Weekly -> "weekly"
        ScanInterval.Monthly -> "monthly"
    }

    fun due(interval: ScanInterval, lastScanAtMs: Long?, nowMs: Long): Boolean =
        CheckSchedule.shouldCheck(checkKey(interval), lastScanAtMs, nowMs)

    fun apply(context: Context, interval: ScanInterval) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        if (interval == ScanInterval.OnDemand) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        val days = when (interval) {
            ScanInterval.Daily -> 1L
            ScanInterval.Weekly -> 7L
            ScanInterval.Monthly -> 30L
            ScanInterval.OnDemand -> return
        }
        val request = PeriodicWorkRequestBuilder<ReleaseRefreshWorker>(days, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
            )
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }
}
