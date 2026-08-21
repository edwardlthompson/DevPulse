package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.staleness.Staleness

object StalenessDays {
    fun of(apps: List<InstalledApp>, nowMs: Long): Map<String, Int?> =
        apps.associate { app ->
            val newest = listOfNotNull(app.remoteReleasedAtMs, app.installedAtMs, app.lastUpdateTimeMs)
                .filter { it > 0L }
                .maxOrNull()
            app.packageName to newest?.let { ((nowMs - it) / Staleness.MS_PER_DAY).toInt() }
        }

    fun encode(days: Map<String, Int?>): Map<String, String> =
        days.mapNotNull { (pkg, value) -> value?.let { pkg to it.toString() } }.toMap()

    fun parse(raw: Map<String, String>): Map<String, Int?> =
        raw.mapValues { it.value.toIntOrNull() }
}