package dev.foss.goldenpath.index.forge

import java.io.File

interface WatchedRepoStore {
    fun load(): List<String>
    fun add(ownerRepo: String)
    fun remove(ownerRepo: String)
}

object WatchedRepoCodec {
    fun encode(all: List<String>): String = buildString {
        all.mapNotNull(::normalize).distinct().forEach { append(it).append('\n') }
    }

    fun decode(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw.lineSequence().mapNotNull(::normalize).distinct().toList()
    }

    fun normalize(ownerRepo: String): String? {
        val repo = ownerRepo.trim().trimStart('/')
        if (!repo.contains('/') || repo.count { it == '/' } != 1) return null
        val owner = repo.substringBefore('/')
        val name = repo.substringAfter('/')
        if (owner.isEmpty() || name.isEmpty()) return null
        return "$owner/$name"
    }
}

class FileWatchedRepoStore(private val file: File) : WatchedRepoStore {
    private val lock = Any()

    override fun load(): List<String> = synchronized(lock) { read() }

    override fun add(ownerRepo: String) {
        val clean = WatchedRepoCodec.normalize(ownerRepo) ?: return
        synchronized(lock) { write((read() + clean).distinct()) }
    }

    override fun remove(ownerRepo: String) {
        val clean = WatchedRepoCodec.normalize(ownerRepo) ?: return
        synchronized(lock) { write(read().filterNot { it.equals(clean, ignoreCase = true) }) }
    }

    private fun read(): List<String> =
        runCatching { WatchedRepoCodec.decode(file.readText(Charsets.UTF_8)) }.getOrDefault(emptyList())

    private fun write(all: List<String>) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(WatchedRepoCodec.encode(all), Charsets.UTF_8)
        }
    }
}
