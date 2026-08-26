package dev.foss.goldenpath.index.forge

import java.io.File

class FileGithubAppOptStore(private val file: File) {
    private val lock = Any()

    fun load(): Map<String, GithubAppOpt> = synchronized(lock) { read() }

    fun put(packageName: String, opt: GithubAppOpt) {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || '.' !in pkg) return
        val clean = GithubAppOpt(opt.includePrereleases, GithubAppOptCodec.regexOrNull(opt.apkRegex)?.pattern)
        synchronized(lock) { write(read() + (pkg to clean)) }
    }

    fun get(packageName: String): GithubAppOpt? = load()[packageName.trim()]

    private fun read(): Map<String, GithubAppOpt> =
        runCatching { decode(file.readText(Charsets.UTF_8)) }.getOrDefault(emptyMap())

    private fun write(all: Map<String, GithubAppOpt>) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(encode(all), Charsets.UTF_8)
        }
    }

    private fun encode(all: Map<String, GithubAppOpt>): String = buildString {
        all.forEach { (pkg, opt) ->
            append(pkg).append('\t')
            append(if (opt.includePrereleases) "1" else "0").append('\t')
            append(opt.apkRegex.orEmpty().replace("\t", " ").replace("\n", " ")).append('\n')
        }
    }

    private fun decode(raw: String): Map<String, GithubAppOpt> {
        if (raw.isBlank()) return emptyMap()
        val out = linkedMapOf<String, GithubAppOpt>()
        raw.lineSequence().forEach { line ->
            val parts = line.split('\t')
            if (parts.size < 2) return@forEach
            val pkg = parts[0].trim()
            if (pkg.isEmpty()) return@forEach
            val regex = parts.getOrNull(2)?.trim()?.takeIf { it.isNotEmpty() && it.length <= GithubAppOptCodec.MAX_REGEX }
            out[pkg] = GithubAppOpt(parts[1].trim() == "1", regex)
        }
        return out
    }
}
