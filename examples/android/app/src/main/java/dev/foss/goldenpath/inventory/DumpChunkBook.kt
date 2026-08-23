package dev.foss.goldenpath.inventory

import java.io.File
import java.util.concurrent.ConcurrentHashMap

object DumpChunkBook {
    private val last = ConcurrentHashMap<String, String>()

    @Volatile
    var persistDir: File? = null

    fun remember(id: String, body: String) {
        if (id.isBlank() || body.isBlank()) return
        last[id] = body
        persistDir?.let { runCatching { File(it, fileName(id)).writeText(body) } }
    }

    fun last(id: String): String? = last[id]

    fun hydrate(dir: File) {
        dir.listFiles { file -> file.name.startsWith("dump_chunk_") && file.name.endsWith(".txt") }
            .orEmpty()
            .forEach { file ->
                val id = file.name.removePrefix("dump_chunk_").removeSuffix(".txt")
                val body = runCatching { file.readText() }.getOrNull() ?: return@forEach
                if (id.isNotBlank() && body.isNotBlank()) last[id] = body
            }
    }

    fun clear() {
        last.clear()
    }

    private fun fileName(id: String): String = "dump_chunk_$id.txt"
}
