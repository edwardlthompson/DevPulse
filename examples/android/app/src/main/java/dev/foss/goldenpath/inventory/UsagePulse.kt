package dev.foss.goldenpath.inventory

data class UsageSnapshot(
    val packageName: String,
    val lastTimeUsedMs: Long,
    val totalTimeInForegroundMs: Long,
)

fun interface UsageCatalog {
    fun usageSince(startMs: Long, endMs: Long): List<UsageSnapshot>
}

object UsagePulse {
    const val WINDOW_MS = 30L * 24 * 60 * 60 * 1000
    private const val MS_PER_HOUR = 3_600_000.0
    private const val MS_PER_DAY = 86_400_000.0

    fun ageDays(app: InstalledApp, nowMs: Long): Double? {
        val ageMs = RemoteRelease.ageMs(app) ?: return null
        return (nowMs - ageMs).coerceAtLeast(0L) / MS_PER_DAY
    }

    fun score(app: InstalledApp, snapshot: UsageSnapshot?, nowMs: Long): Double {
        val days = ageDays(app, nowMs) ?: return 0.0
        val hours = (snapshot?.totalTimeInForegroundMs ?: 0L) / MS_PER_HOUR
        return hours * days
    }
}
