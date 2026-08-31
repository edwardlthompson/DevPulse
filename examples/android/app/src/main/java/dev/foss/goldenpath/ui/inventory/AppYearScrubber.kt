package dev.foss.goldenpath.ui.inventory

import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import java.util.Calendar

data class YearKeypoint(
    val year: String,
    val index: Int,
    val fraction: Float,
)

object AppYearScrubber {

    fun appTimestampMs(app: InstalledApp): Long? {
        val remoteMs = app.remoteReleasedAtMs
        if (remoteMs != null && remoteMs > 0L && app.remoteReleasedSource != RemoteReleasedSource.None) {
            return remoteMs
        }
        val installedMs = app.installedAtMs
        if (installedMs != null && installedMs > 0L) {
            return installedMs
        }
        if (app.lastUpdateTimeMs > 0L) {
            return app.lastUpdateTimeMs
        }
        if (app.firstInstallTimeMs > 0L) {
            return app.firstInstallTimeMs
        }
        return null
    }

    fun appYear(app: InstalledApp): Int? {
        val ms = appTimestampMs(app) ?: return null
        val cal = Calendar.getInstance().apply { timeInMillis = ms }
        val year = cal.get(Calendar.YEAR)
        return if (year in 2000..2100) year else null
    }

    fun yearLabel(app: InstalledApp): String {
        val y = appYear(app) ?: return "—"
        return y.toString()
    }

    fun yearForIndex(apps: List<InstalledApp>, index: Int): String {
        if (apps.isEmpty() || index !in apps.indices) return "—"
        return yearLabel(apps[index])
    }

    fun findYearKeypoints(apps: List<InstalledApp>): List<YearKeypoint> {
        if (apps.isEmpty()) return emptyList()
        val total = apps.size
        val result = mutableListOf<YearKeypoint>()
        var lastYear: String? = null

        for (i in apps.indices) {
            val yr = yearLabel(apps[i])
            if (yr != lastYear && yr != "—") {
                val frac = if (total > 1) i.toFloat() / (total - 1) else 0f
                result.add(YearKeypoint(year = yr, index = i, fraction = frac))
                lastYear = yr
            }
        }
        return result
    }

    fun targetIndexForFraction(fraction: Float, totalItems: Int): Int {
        if (totalItems <= 0) return 0
        if (totalItems == 1) return 0
        return ((totalItems - 1) * fraction.coerceIn(0f, 1f)).toInt().coerceIn(0, totalItems - 1)
    }
}
