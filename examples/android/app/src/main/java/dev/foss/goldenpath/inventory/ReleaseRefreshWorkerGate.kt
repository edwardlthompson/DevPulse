package dev.foss.goldenpath.inventory

/** Pure preflight for background ReleaseRefreshWorker before tryBegin. */
enum class ReleaseRefreshWorkerOutcome {
    /** QUERY_ALL not acknowledged — do not retry until the user grants it. */
    SkipSuccess,
    /** Wi-Fi-only on metered network — retry when constraints allow. */
    RetryLater,
    Ready,
}

object ReleaseRefreshWorkerGate {
    fun decide(
        canScan: Boolean,
        wifiOnly: Boolean,
        unmetered: Boolean,
    ): ReleaseRefreshWorkerOutcome = when {
        !canScan -> ReleaseRefreshWorkerOutcome.SkipSuccess
        !RefreshWifiOnly.allow(wifiOnly, unmetered) -> ReleaseRefreshWorkerOutcome.RetryLater
        else -> ReleaseRefreshWorkerOutcome.Ready
    }
}
