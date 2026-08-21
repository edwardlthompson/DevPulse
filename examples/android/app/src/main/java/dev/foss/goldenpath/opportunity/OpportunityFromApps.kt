package dev.foss.goldenpath.opportunity

import dev.foss.goldenpath.inventory.AppOrigin
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.StalenessDays
import dev.foss.goldenpath.staleness.Badge
import dev.foss.goldenpath.staleness.Staleness

object OpportunityFromApps {
    fun quietApps(apps: List<InstalledApp>, nowMs: Long, includePinned: Boolean, pins: Set<String>): List<InstalledApp> {
        val days = StalenessDays.of(apps, nowMs)
        return apps.filter { app ->
            val age = days[app.packageName] ?: return@filter false
            Staleness.badgeForDays(age) == Badge.Red &&
                (includePinned || app.packageName !in pins)
        }
    }

    fun gaps(
        apps: List<InstalledApp>,
        nowMs: Long,
        includePinned: Boolean,
        pins: Set<String>,
        categories: Map<String, String> = emptyMap(),
    ): List<CategoryGap> {
        val quiet = quietApps(apps, nowMs, includePinned, pins)
        val byPackage = quiet.associate { it.packageName to categoryOf(it, categories) }
        return OpportunityRanker.gaps(byPackage, quiet.map { it.packageName }.toSet())
    }

    fun categoryOf(app: InstalledApp, categories: Map<String, String> = emptyMap()): String {
        val stored = categories[app.packageName]?.trim()?.ifEmpty { null }
        if (stored != null) return stored
        return when (app.origin) {
            AppOrigin.Fdroid -> "F-Droid"
            AppOrigin.Play -> "Play"
            AppOrigin.ExtraRepo -> "Extra repo"
            AppOrigin.Unknown, AppOrigin.SideloadedUnknown -> "Unknown"
        }
    }
}
