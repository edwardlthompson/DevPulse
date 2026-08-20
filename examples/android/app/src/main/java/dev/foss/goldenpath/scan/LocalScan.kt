package dev.foss.goldenpath.scan

import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.staleness.LocalOnlyStaleness

object LocalScan {
    fun run(apps: List<InstalledApp>, nowMs: Long): List<ScanItem> =
        apps.map { app ->
            ScanItem(
                app = app,
                staleness = LocalOnlyStaleness.evaluate(app, nowMs),
                repoFound = false,
            )
        }
}
