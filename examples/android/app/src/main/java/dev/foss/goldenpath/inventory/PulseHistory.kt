package dev.foss.goldenpath.inventory

import java.io.File

data class PulseHistoryRow(
    val atMs: Long,
    val kind: String,
    val wallMs: Long,
    val count: Int,
    val extra: String = "",
)

object PulseHistory {
    const val RETAIN_MS = 30L * 24 * 60 * 60 * 1000
    const val FILE = "pulse_log.tsv"

    fun file(filesDir: File): File = File(File(filesDir, "scan-history"), FILE)

    fun note(filesDir: File, kind: String, wallMs: Long, count: Int, extra: String = "", nowMs: Long = System.currentTimeMillis()) {
        append(file(filesDir), PulseHistoryRow(nowMs, kind, wallMs.coerceAtLeast(0), count.coerceAtLeast(0), extra), nowMs)
    }

    fun append(file: File, row: PulseHistoryRow, nowMs: Long = row.atMs) {
        val kept = (load(file) + row).filter { nowMs - it.atMs <= RETAIN_MS }
        file.parentFile?.mkdirs()
        runCatching { file.writeText(kept.joinToString("\n") { encode(it) }) }
    }

    fun load(file: File): List<PulseHistoryRow> {
        if (!file.isFile) return emptyList()
        return runCatching { file.readLines().mapNotNull(::parse) }.getOrDefault(emptyList())
    }

    internal fun parse(line: String): PulseHistoryRow? {
        val parts = line.split('\t')
        if (parts.size < 4) return null
        val at = parts[0].toLongOrNull() ?: return null
        val kind = parts[1].trim()
        val wall = parts[2].toLongOrNull() ?: return null
        val count = parts[3].toIntOrNull() ?: return null
        if (kind.isEmpty()) return null
        return PulseHistoryRow(at, kind, wall, count, parts.getOrElse(4) { "" })
    }

    internal fun encode(row: PulseHistoryRow): String =
        listOf(row.atMs, row.kind, row.wallMs, row.count, row.extra).joinToString("\t")
}
