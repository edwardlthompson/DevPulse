package dev.foss.goldenpath.query

import dev.foss.goldenpath.inventory.AppOrigin
import dev.foss.goldenpath.scan.ScanItem
import dev.foss.goldenpath.staleness.Badge

data class ScanQuery(
    val maxAgeDays: Int? = null,
    val origin: AppOrigin? = null,
    val hasPublicRepo: Boolean? = null,
    val hidePinnedFromRed: Boolean = true,
)

data class HistoryEntry(
    val atMs: Long,
    val redCount: Int,
    val unknownCount: Int,
)

object PinRules {
    fun hideFromRedList(packageName: String, badge: Badge, pins: Set<String>): Boolean =
        badge == Badge.Red && packageName in pins
}

object ScanQueryEngine {
    fun filter(items: List<ScanItem>, pins: Set<String>, query: ScanQuery): List<ScanItem> =
        items.filter { item ->
            val days = item.staleness.daysSinceActivity
            val ageOk = query.maxAgeDays == null || (days != null && days <= query.maxAgeDays)
            val originOk = query.origin == null || item.app.origin == query.origin
            val repoOk = query.hasPublicRepo == null || item.repoFound == query.hasPublicRepo
            val pinOk = !query.hidePinnedFromRed ||
                !PinRules.hideFromRedList(item.app.packageName, item.staleness.badge, pins)
            ageOk && originOk && repoOk && pinOk
        }
}

object ScanExport {
    fun toCsv(items: List<ScanItem>): String {
        val header = "package,label,badge,repoFound"
        val rows = items.joinToString("\n") { item ->
            listOf(
                item.app.packageName,
                item.app.label.replace(',', ' '),
                item.staleness.badge.name,
                item.repoFound.toString(),
            ).joinToString(",")
        }
        return if (rows.isEmpty()) header else "$header\n$rows"
    }

    fun toJson(items: List<ScanItem>): String =
        "[" + items.joinToString(",") { item ->
            """{"package":"${item.app.packageName}","badge":"${item.staleness.badge.name}"}"""
        } + "]"
}

object ScanHistory {
    fun entry(atMs: Long, items: List<ScanItem>): HistoryEntry = HistoryEntry(
        atMs = atMs,
        redCount = items.count { it.staleness.badge == Badge.Red },
        unknownCount = items.count { it.staleness.badge == Badge.Unknown },
    )
}
