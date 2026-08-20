package dev.foss.goldenpath.inventory

import android.app.usage.UsageStatsManager

class UsageStatsManagerCatalog(
    private val manager: UsageStatsManager?,
) : UsageCatalog {
    override fun usageSince(startMs: Long, endMs: Long): List<UsageSnapshot> {
        val usageManager = manager ?: return emptyList()
        return try {
            val rows = usageManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMs, endMs)
                ?: return emptyList()
            rows.groupBy { it.packageName }.map { (packageName, group) ->
                UsageSnapshot(
                    packageName = packageName,
                    lastTimeUsedMs = group.maxOf { it.lastTimeUsed },
                    totalTimeInForegroundMs = group.sumOf { it.totalTimeInForeground },
                )
            }
        } catch (_: SecurityException) {
            emptyList()
        }
    }
}
