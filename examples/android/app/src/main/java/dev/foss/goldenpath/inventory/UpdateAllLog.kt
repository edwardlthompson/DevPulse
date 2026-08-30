package dev.foss.goldenpath.inventory

import java.io.File

data class UpdateAllLogRow(
    val atMs: Long,
    val packageName: String,
    val label: String,
    val source: String,
    val result: String,
    val why: String,
)

object UpdateAllLog {
    const val FILE = "update_all_log.tsv"
    const val MAX = 120

    fun file(filesDir: File): File = File(filesDir, FILE)

    fun note(
        filesDir: File,
        job: UpdateAllJob,
        result: String,
        why: String,
        atMs: Long = System.currentTimeMillis(),
    ) {
        append(
            file(filesDir),
            UpdateAllLogRow(atMs, job.packageName, job.label, job.source.name, result, why),
        )
    }

    fun load(file: File): List<UpdateAllLogRow> {
        if (!file.isFile) return emptyList()
        return runCatching { file.readLines().mapNotNull(::parse) }.getOrDefault(emptyList())
    }

    internal fun parse(line: String): UpdateAllLogRow? {
        val parts = line.split('\t')
        if (parts.size < 6) return null
        val at = parts[0].toLongOrNull() ?: return null
        val pkg = parts[1].trim()
        val source = parts[3].trim()
        val result = parts[4].trim()
        if (pkg.isEmpty() || source.isEmpty() || result.isEmpty()) return null
        return UpdateAllLogRow(at, pkg, parts[2].trim(), source, result, parts[5].trim())
    }

    internal fun encode(row: UpdateAllLogRow): String =
        listOf(row.atMs, row.packageName, row.label, row.source, row.result, row.why).joinToString("\t")

    private fun append(file: File, row: UpdateAllLogRow) {
        replace(file, (load(file) + row).takeLast(MAX))
    }

    fun replace(file: File, rows: List<UpdateAllLogRow>) {
        file.parentFile?.mkdirs()
        runCatching { file.writeText(rows.joinToString("\n") { encode(it) }) }
    }

    fun failed(file: File): List<UpdateAllLogRow> =
        load(file).filter { it.result == "failDl" || it.result == "failIns" }
}
