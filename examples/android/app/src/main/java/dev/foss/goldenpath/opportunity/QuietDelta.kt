package dev.foss.goldenpath.opportunity

import java.io.File

object QuietDelta {
    fun count(previous: Set<String>, current: Set<String>): Int =
        current.count { it.isNotBlank() && it !in previous }

    fun load(file: File): Set<String> =
        runCatching { file.readLines().map { it.trim() }.filter { it.isNotEmpty() }.toSet() }
            .getOrDefault(emptySet())

    fun save(file: File, current: Set<String>) {
        runCatching { file.writeText(current.filter { it.isNotBlank() }.sorted().joinToString("\n")) }
    }
}
