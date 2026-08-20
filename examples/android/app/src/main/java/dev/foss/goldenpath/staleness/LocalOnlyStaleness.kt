package dev.foss.goldenpath.staleness

import dev.foss.goldenpath.inventory.InstalledApp

object LocalOnlyStaleness {
    fun evaluate(app: InstalledApp, nowMs: Long): StalenessResult = Staleness.evaluate(
        StalenessInput(
            remotes = emptyList(),
            installedLastUpdateMs = app.installedAtMs ?: 0L,
            targetSdk = app.targetSdk,
        ),
        nowMs,
    )
}
