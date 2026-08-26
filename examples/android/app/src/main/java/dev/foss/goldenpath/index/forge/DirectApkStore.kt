package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.ApkDownloadUrl
import java.io.File

object DirectApkCodec {
    fun normalize(packageName: String, url: String): Pair<String, String>? {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || '.' !in pkg) return null
        val href = ApkDownloadUrl.httpsFile(url) ?: return null
        return pkg to href
    }

    fun encode(all: Map<String, String>): String = buildString {
        all.forEach { (pkg, url) ->
            val clean = normalize(pkg, url) ?: return@forEach
            append(clean.first).append('\t').append(clean.second).append('\n')
        }
    }

    fun decode(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        val out = linkedMapOf<String, String>()
        raw.lineSequence().forEach { line ->
            val tab = line.indexOf('\t')
            if (tab <= 0) return@forEach
            normalize(line.substring(0, tab), line.substring(tab + 1))?.let { out[it.first] = it.second }
        }
        return out
    }
}

class FileDirectApkStore(private val file: File) {
    private val lock = Any()

    fun load(): Map<String, String> = synchronized(lock) { read() }

    fun put(packageName: String, url: String) {
        val clean = DirectApkCodec.normalize(packageName, url) ?: return
        synchronized(lock) { write(read() + (clean.first to clean.second)) }
    }

    private fun read(): Map<String, String> =
        runCatching { DirectApkCodec.decode(file.readText(Charsets.UTF_8)) }.getOrDefault(emptyMap())

    private fun write(all: Map<String, String>) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(DirectApkCodec.encode(all), Charsets.UTF_8)
        }
    }
}
