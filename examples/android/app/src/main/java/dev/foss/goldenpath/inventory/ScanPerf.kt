package dev.foss.goldenpath.inventory

/** Local scan performance markers for docs/PERF.md budgets. */
data class ScanPerfSnapshot(
    val appCount: Int,
    val coldStartMs: Long,
    val durationMs: Long,
    val peakMemoryBytes: Long,
)

object ScanPerf {
    @Volatile var coldStartMs: Long = 0L
    @Volatile var scanStartedAtMs: Long = 0L
    @Volatile var peakMemoryBytes: Long = 0L
    @Volatile var last: ScanPerfSnapshot? = null

    fun markColdStart(elapsedMs: Long) {
        coldStartMs = elapsedMs.coerceAtLeast(0L)
    }

    fun beginScan() {
        scanStartedAtMs = System.currentTimeMillis()
        peakMemoryBytes = usedMemoryBytes()
    }

    fun sampleMemory() {
        val used = usedMemoryBytes()
        if (used > peakMemoryBytes) peakMemoryBytes = used
    }

    fun finishScan(appCount: Int): ScanPerfSnapshot {
        sampleMemory()
        val duration = (System.currentTimeMillis() - scanStartedAtMs).coerceAtLeast(0L)
        val snap = ScanPerfSnapshot(
            appCount = appCount.coerceAtLeast(0),
            coldStartMs = coldStartMs,
            durationMs = duration,
            peakMemoryBytes = peakMemoryBytes,
        )
        last = snap
        return snap
    }

    fun usedMemoryBytes(): Long {
        val runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()).coerceAtLeast(0L)
    }

    fun reset() {
        coldStartMs = 0L
        scanStartedAtMs = 0L
        peakMemoryBytes = 0L
        last = null
    }
}
