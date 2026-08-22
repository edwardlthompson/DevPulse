package dev.foss.goldenpath.inventory

import java.io.File

object RefreshPaceFile {
    fun load(file: File): Map<String, Long> {
        if (!file.isFile) return emptyMap()
        return runCatching {
            file.readLines().mapNotNull { line ->
                val parts = line.split('\t')
                if (parts.size < 2) return@mapNotNull null
                val ms = parts[1].toLongOrNull() ?: return@mapNotNull null
                parts[0] to ms
            }.toMap()
        }.getOrDefault(emptyMap())
    }

    fun save(file: File, prior: Map<String, Long>) {
        val body = prior.entries.sortedBy { it.key }.joinToString("\n") { "${it.key}\t${it.value}" }
        runCatching { file.writeText(body) }
    }
}

object RefreshOutletEta {
    fun label(ms: Long?): String {
        if (ms == null) return ""
        val elapsed = ms.coerceAtLeast(0L)
        if (elapsed < 1_000L) return "${elapsed}ms"
        val totalSec = elapsed / 1_000L
        val min = totalSec / 60L
        val sec = totalSec % 60L
        return if (min <= 0L) "${sec}s" else "${min}m ${sec}s"
    }

    fun sorted(outlets: List<RefreshOutletSnap>): List<RefreshOutletSnap> {
        val done = outlets.filter { finished(it) }
            .sortedWith(compareByDescending<RefreshOutletSnap> { it.finishedAtMs ?: 0L }.thenBy { it.id })
        return done + outlets.filter { !finished(it) }
    }

    internal fun finished(row: RefreshOutletSnap): Boolean =
        row.skipped || (row.total > 0 && row.done >= row.total)
}
