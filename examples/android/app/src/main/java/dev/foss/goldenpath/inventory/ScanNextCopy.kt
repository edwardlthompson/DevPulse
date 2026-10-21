package dev.foss.goldenpath.inventory

/** Formats the Settings "next background scan" line from schedule math. */
object ScanNextCopy {
    private const val MS_HOUR = 3_600_000L

    sealed class Kind {
        data object Hidden : Kind()
        data object Soon : Kind()
        data class Hours(val hours: Int) : Kind()
        data class Days(val days: Int) : Kind()
    }

    fun kind(interval: ScanInterval, lastScanAtMs: Long?, nowMs: Long): Kind {
        if (interval == ScanInterval.OnDemand) return Kind.Hidden
        val delay = ScanSchedule.nextDelayMs(interval, lastScanAtMs, nowMs)
        if (delay <= 0L) return Kind.Soon
        val hours = ((delay + MS_HOUR - 1) / MS_HOUR).toInt().coerceAtLeast(1)
        if (hours < 48) return Kind.Hours(hours)
        val days = ((delay + ScanSchedule.periodMs(ScanInterval.Daily) - 1) /
            ScanSchedule.periodMs(ScanInterval.Daily)).toInt().coerceAtLeast(1)
        return Kind.Days(days)
    }
}
