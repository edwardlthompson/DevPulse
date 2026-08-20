package dev.foss.goldenpath.index.forge

import java.io.File

interface GithubVerifiedStore {
    fun load(): Map<String, String>
    fun save(all: Map<String, String>)
    fun put(packageName: String, ownerRepo: String)
}

object GithubVerifiedCodec {
    fun encode(all: Map<String, String>): String = buildString {
        all.forEach { (pkg, ownerRepo) ->
            if (pkg.isBlank() || ownerRepo.isBlank()) return@forEach
            append(esc(pkg)).append('\t').append(esc(ownerRepo)).append('\n')
        }
    }

    fun decode(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        val out = linkedMapOf<String, String>()
        raw.lineSequence().forEach { line ->
            val tab = line.indexOf('\t')
            if (tab <= 0) return@forEach
            val pkg = unesc(line.substring(0, tab))
            val ownerRepo = unesc(line.substring(tab + 1))
            if (pkg.isEmpty() || !ownerRepo.contains('/')) return@forEach
            out[pkg] = ownerRepo
        }
        return out
    }

    private fun esc(value: String): String = value.replace("\t", " ").replace("\n", " ")
    private fun unesc(value: String): String = value.trim()
}

class FileGithubVerifiedStore(private val file: File) : GithubVerifiedStore {
    private val lock = Any()

    override fun load(): Map<String, String> = synchronized(lock) { read() }

    override fun save(all: Map<String, String>) {
        synchronized(lock) { write(all) }
    }

    override fun put(packageName: String, ownerRepo: String) {
        val pkg = packageName.trim()
        val repo = ownerRepo.trim()
        if (pkg.isEmpty() || !repo.contains('/')) return
        synchronized(lock) { write(read() + (pkg to repo)) }
    }

    private fun read(): Map<String, String> =
        runCatching { GithubVerifiedCodec.decode(file.readText(Charsets.UTF_8)) }.getOrDefault(emptyMap())

    private fun write(all: Map<String, String>) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(GithubVerifiedCodec.encode(all), Charsets.UTF_8)
        }
    }
}
