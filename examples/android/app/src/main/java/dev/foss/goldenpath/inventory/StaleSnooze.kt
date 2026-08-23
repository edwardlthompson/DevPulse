package dev.foss.goldenpath.inventory

import java.io.File

object StaleSnooze {
    const val MS = 7L * 86_400_000L

    fun until(nowMs: Long): Long = nowMs + MS

    fun hidden(untilMs: Long?, nowMs: Long): Boolean = (untilMs ?: 0L) > nowMs

    fun load(file: File): Map<String, Long> = RefreshPaceFile.load(file)

    fun save(file: File, rows: Map<String, Long>) {
        RefreshPaceFile.save(file, rows)
    }
}
