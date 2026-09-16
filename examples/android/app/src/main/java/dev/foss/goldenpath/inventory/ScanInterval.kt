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
    private const val MS_DAY = 86_400_000L

    fun checkKey(interval: ScanInterval): String = when (interval) {
        ScanInterval.OnDemand -> "off"
        ScanInterval.Daily -> "daily"
        ScanInterval.Weekly -> "weekly"
        ScanInterval.Monthly -> "monthly"
    }

    fun periodMs(interval: ScanInterval): Long = when (interval) {
        ScanInterval.OnDemand -> 0L
        ScanInterval.Daily -> MS_DAY
        ScanInterval.Weekly -> 7 * MS_DAY
        ScanInterval.Monthly -> 30 * MS_DAY
    }

    fun due(interval: ScanInterval, lastScanAtMs: Long?, nowMs: Long): Boolean =
        CheckSchedule.shouldCheck(checkKey(interval), lastScanAtMs, nowMs)

    fun nextDelayMs(interval: ScanInterval, lastScanAtMs: Long?, nowMs: Long): Long {
        val period = periodMs(interval)
        if (period <= 0L) return 0L
        if (lastScanAtMs == null) return period
        return (period - (nowMs - lastScanAtMs)).coerceAtLeast(0L)
    }

    fun apply(
        context: Context,
        interval: ScanInterval,
        lastScanAtMs: Long? = null,
        nowMs: Long = System.currentTimeMillis(),
        replace: Boolean = false,
    ) {
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
            .setInitialDelay(nextDelayMs(interval, lastScanAtMs, nowMs), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
            )
            .build()
        val policy = if (replace) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP
        workManager.enqueueUniquePeriodicWork(WORK_NAME, policy, request)
    }
}
