package dev.foss.goldenpath.inventory

import java.io.File
import java.util.concurrent.ConcurrentHashMap

object RefreshResume {
    const val FILE = "refresh_done.txt"

    @Volatile
    var persistDir: File? = null

    private val done = ConcurrentHashMap.newKeySet<String>()

    fun leftover(planned: Collection<String>, finished: Collection<String>): List<String> {
        val have = finished.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        return planned.map { it.trim() }.filter { it.isNotEmpty() && it !in have }
    }

    fun hydrate(ids: Collection<String>) {
        done.clear()
        ids.map { it.trim() }.filter { it.isNotEmpty() }.forEach { done.add(it) }
    }

    fun load(dir: File): Set<String> =
        File(dir, FILE).takeIf { it.isFile }?.readLines(Charsets.UTF_8)
            ?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet().orEmpty()

    fun remember(id: String) {
        val key = id.trim()
        if (key.isEmpty()) return
        done.add(key)
        persist()
    }

    fun apply(planned: Collection<String>): Boolean {
        val names = planned.map { it.trim() }.filter { it.isNotEmpty() }
        val hit = names.count { it in done }
        if (hit == 0) return false
        if (names.isNotEmpty() && hit >= names.size) {
            clear()
            return false
        }
        done.forEach { RefreshSkip.stop(it) }
        return true
    }

    fun clear() {
        done.clear()
        persistDir?.let { File(it, FILE).delete() }
    }

    fun snapshot(): Set<String> = done.toSet()

    private fun persist() {
        val dir = persistDir ?: return
        if (done.isEmpty()) File(dir, FILE).delete()
        else File(dir, FILE).writeText(done.sorted().joinToString("\n"), Charsets.UTF_8)
    }
}
