package dev.foss.goldenpath.inventory

import java.io.File

object UpdateAllResume {
    const val FILE = "update_all_queue.txt"

    fun leftover(
        open: List<Pair<String, List<UpdateAllJob>>>,
        settled: Set<String>,
    ): List<String> = open.mapNotNull { (pkg, group) ->
        if (pkg.isEmpty() || pkg in settled || group.isEmpty()) null else pkg
    }

    fun checkpoint(
        filesDir: File?,
        open: List<Pair<String, List<UpdateAllJob>>>,
        settled: Set<String>,
    ) {
        if (filesDir == null) return
        val left = leftover(open, settled)
        if (left.isEmpty()) File(filesDir, FILE).delete() else {
            File(filesDir, FILE).writeText(left.joinToString("\n"), Charsets.UTF_8)
        }
    }

    fun load(filesDir: File): List<String> =
        File(filesDir, FILE).takeIf { it.isFile }?.readLines(Charsets.UTF_8)
            ?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
}
