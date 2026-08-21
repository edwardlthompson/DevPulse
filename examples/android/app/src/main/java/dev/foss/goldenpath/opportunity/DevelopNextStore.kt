package dev.foss.goldenpath.opportunity

import java.io.File

interface DevelopNextStore {
    fun load(): Map<String, String>
    fun put(packageName: String, note: String)
}

object DevelopNextCodec {
    fun encode(all: Map<String, String>): String = buildString {
        all.forEach { (pkg, note) ->
            val clean = normalize(pkg, note) ?: return@forEach
            append(esc(clean.first)).append('\t').append(esc(clean.second)).append('\n')
        }
    }

    fun decode(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        val out = linkedMapOf<String, String>()
        raw.lineSequence().forEach { line ->
            val tab = line.indexOf('\t')
            if (tab <= 0) return@forEach
            normalize(unesc(line.substring(0, tab)), unesc(line.substring(tab + 1)))?.let { out[it.first] = it.second }
        }
        return out
    }

    fun notes(all: Map<String, String>): List<DevelopNextNote> =
        all.map { DevelopNextNote(it.key, it.value) }

    fun normalize(packageName: String, note: String): Pair<String, String>? {
        val pkg = packageName.trim()
        val text = note.trim()
        if (pkg.isEmpty() || text.isEmpty()) return null
        return pkg to text
    }

    private fun esc(value: String): String = value.replace("\t", " ").replace("\n", " ")
    private fun unesc(value: String): String = value.trim()
}

class FileDevelopNextStore(private val file: File) : DevelopNextStore {
    private val lock = Any()

    override fun load(): Map<String, String> = synchronized(lock) { read() }

    override fun put(packageName: String, note: String) {
        synchronized(lock) {
            val current = read().toMutableMap()
            val clean = DevelopNextCodec.normalize(packageName, note)
            if (clean == null) current.remove(packageName.trim()) else current[clean.first] = clean.second
            write(current)
        }
    }

    private fun read(): Map<String, String> =
        runCatching { DevelopNextCodec.decode(file.readText(Charsets.UTF_8)) }.getOrDefault(emptyMap())

    private fun write(all: Map<String, String>) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(DevelopNextCodec.encode(all), Charsets.UTF_8)
        }
    }
}
