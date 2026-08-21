package dev.foss.goldenpath.index.forge

import java.io.File

interface PastedRepoStore {
    fun load(): Map<String, String>
    fun save(all: Map<String, String>)
    fun put(packageName: String, url: String)
}

object PastedRepoCodec {
    fun encode(all: Map<String, String>): String = buildString {
        all.forEach { (pkg, url) ->
            val clean = normalize(pkg, url) ?: return@forEach
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

    fun normalize(packageName: String, url: String): Pair<String, String>? {
        val pkg = packageName.trim()
        val href = url.trim()
        if (pkg.isEmpty() || '.' !in pkg) return null
        if (!href.startsWith("https://") && !href.startsWith("http://")) return null
        return pkg to href
    }

    fun hints(pasted: Map<String, String>): Map<String, GithubHint> =
        pasted.mapNotNull { (pkg, url) ->
            FdroidGithubHints.ownerRepo(url)?.let { pkg to GithubHint(it) }
        }.toMap()

    private fun esc(value: String): String = value.replace("\t", " ").replace("\n", " ")
    private fun unesc(value: String): String = value.trim()
}

class FilePastedRepoStore(private val file: File) : PastedRepoStore {
    private val lock = Any()

    override fun load(): Map<String, String> = synchronized(lock) { read() }

    override fun save(all: Map<String, String>) {
        synchronized(lock) { write(all) }
    }

    override fun put(packageName: String, url: String) {
        val clean = PastedRepoCodec.normalize(packageName, url) ?: return
        synchronized(lock) { write(read() + (clean.first to clean.second)) }
    }

    private fun read(): Map<String, String> =
        runCatching { PastedRepoCodec.decode(file.readText(Charsets.UTF_8)) }.getOrDefault(emptyMap())

    private fun write(all: Map<String, String>) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(PastedRepoCodec.encode(all), Charsets.UTF_8)
        }
    }
}
