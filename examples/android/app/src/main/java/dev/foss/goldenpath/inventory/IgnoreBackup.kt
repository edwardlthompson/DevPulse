package dev.foss.goldenpath.inventory

import java.io.File

object IgnoreBackup {
    fun export(filesDir: File): String {
        val ignore = IgnoredUpdates.file(filesDir).takeIf { it.isFile }?.readText().orEmpty()
        val applied = File(filesDir, "applied_updates.txt").takeIf { it.isFile }?.readText().orEmpty()
        return "#ignore\n$ignore\n#applied\n$applied"
    }

    fun restore(filesDir: File, raw: String) {
        val ignore = StringBuilder()
        val applied = StringBuilder()
        var target = ignore
        raw.lineSequence().forEach { line ->
            when (line.trim()) {
                "#ignore" -> target = ignore
                "#applied" -> target = applied
                else -> if (line.isNotBlank()) target.append(line).append('\n')
            }
        }
        runCatching { IgnoredUpdates.file(filesDir).writeText(ignore.toString()) }
        runCatching { File(filesDir, "applied_updates.txt").writeText(applied.toString()) }
        IgnoredUpdates.hydrate(filesDir)
    }
}
