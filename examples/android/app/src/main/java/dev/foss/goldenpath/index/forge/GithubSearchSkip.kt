package dev.foss.goldenpath.index.forge

import java.io.File

object GithubSearchSkip {
    const val TTL_MS = 30L * 24 * 60 * 60 * 1000

    fun encode(all: Map<String, Long>): String = buildString {
        all.forEach { (pkg, at) ->
            if (pkg.isBlank()) return@forEach
            append(pkg.replace('\t', ' ').replace('\n', ' ')).append('\t').append(at).append('\n')
        }
    }

    fun decode(raw: String): Map<String, Long> {
        if (raw.isBlank()) return emptyMap()
        val out = linkedMapOf<String, Long>()
        raw.lineSequence().forEach { line ->
            val tab = line.indexOf('\t')
            if (tab <= 0) return@forEach
            val pkg = line.substring(0, tab).trim()
            val at = line.substring(tab + 1).trim().toLongOrNull() ?: return@forEach
            if (pkg.isNotEmpty()) out[pkg] = at
        }
        return out
    }

    fun blocked(packageName: String, all: Map<String, Long>, nowMs: Long): Boolean {
        val at = all[packageName.trim()] ?: return false
        return nowMs - at < TTL_MS
    }

    fun remember(all: Map<String, Long>, packageName: String, nowMs: Long): Map<String, Long> {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return all
        return all + (pkg to nowMs)
    }
}

class FileGithubSearchSkipStore(private val file: File) {
    private val lock = Any()

    fun load(): Map<String, Long> = synchronized(lock) {
        runCatching { GithubSearchSkip.decode(file.readText(Charsets.UTF_8)) }.getOrDefault(emptyMap())
    }

    fun save(all: Map<String, Long>) {
        synchronized(lock) {
            runCatching {
                file.parentFile?.mkdirs()
                file.writeText(GithubSearchSkip.encode(all), Charsets.UTF_8)
            }
        }
    }
}
