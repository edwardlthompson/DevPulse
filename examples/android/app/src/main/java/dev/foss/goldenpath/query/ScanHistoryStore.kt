package dev.foss.goldenpath.query

import dev.foss.goldenpath.scan.ScanItem
import java.io.File

object ScanHistoryCodec {
    fun badgesLine(packageName: String, badge: String): String? {
        val pkg = packageName.trim()
        val name = badge.trim()
        if (pkg.isEmpty() || name.isEmpty()) return null
        return "$pkg\t$name"
    }

    fun parseBadges(raw: String): Map<String, String> =
        raw.lineSequence().mapNotNull { line ->
            val parts = line.split('\t')
            if (parts.size < 2) return@mapNotNull null
            val pkg = parts[0].trim()
            val badge = parts[1].trim()
            if (pkg.isEmpty() || badge.isEmpty()) null else pkg to badge
        }.toMap()

    fun wentQuiet(previous: Map<String, String>, current: Map<String, String>): List<String> =
        current.filter { (pkg, badge) ->
            badge == "Red" && previous[pkg] != null && previous[pkg] != "Red"
        }.keys.sorted()
}

object ScanHistoryWrite {
    fun afterScan(dir: File, items: List<ScanItem>) {
        val store = ScanHistoryStore(dir)
        val current = items.associate { it.app.packageName to it.staleness.badge.name }
        val quiet = ScanHistoryCodec.wentQuiet(store.loadBadges(), current)
        store.saveQuiet(quiet)
        store.saveBadges(current)
    }
}

class ScanHistoryStore(private val dir: File) {
    fun loadBadges(): Map<String, String> =
        runCatching { ScanHistoryCodec.parseBadges(badgesFile().readText()) }.getOrDefault(emptyMap())

    fun saveBadges(badges: Map<String, String>) {
        dir.mkdirs()
        badgesFile().writeText(
            badges.entries.mapNotNull { ScanHistoryCodec.badgesLine(it.key, it.value) }.joinToString("\n"),
        )
    }

    fun lastQuiet(): List<String> =
        runCatching { quietFile().readText().lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList() }
            .getOrDefault(emptyList())

    fun saveQuiet(packages: List<String>) {
        dir.mkdirs()
        quietFile().writeText(packages.joinToString("\n"))
    }

    fun loadDays(): Map<String, String> =
        runCatching { ScanHistoryCodec.parseBadges(daysFile().readText()) }.getOrDefault(emptyMap())

    fun saveDays(days: Map<String, String>) {
        dir.mkdirs()
        daysFile().writeText(
            days.entries.mapNotNull { ScanHistoryCodec.badgesLine(it.key, it.value) }.joinToString("\n"),
        )
    }

    private fun badgesFile() = File(dir, "scan_badges.tsv")
    private fun quietFile() = File(dir, "scan_quiet.txt")
    private fun daysFile() = File(dir, "scan_days.tsv")
}
