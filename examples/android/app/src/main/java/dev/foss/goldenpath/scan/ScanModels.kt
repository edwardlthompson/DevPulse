package dev.foss.goldenpath.scan

import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.staleness.RemoteSource
import dev.foss.goldenpath.staleness.StalenessResult

enum class ScanPhase {
    Idle,
    Running,
    Paused,
    Completed,
}

data class ScanProgress(
    val phase: ScanPhase,
    val completed: Int,
    val total: Int,
)

data class ScanItem(
    val app: InstalledApp,
    val staleness: StalenessResult,
    val repoFound: Boolean = false,
)

data class ScanDetail(
    val item: ScanItem,
    val remoteDates: Map<RemoteSource, Long?> = emptyMap(),
    val notes: String = "",
)
