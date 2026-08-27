package dev.foss.goldenpath.index.forge

import java.io.ByteArrayOutputStream
import java.io.InputStream

object ObtainiumImport {
    const val MAX_BYTES = 2 * 1024 * 1024

    data class Result(val imported: Int, val skipped: Int, val rows: List<Pair<String, String>>)

    fun readUtf8(stream: InputStream, maxBytes: Int = MAX_BYTES): String? {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(8192)
        var total = 0
        while (true) {
            val n = stream.read(buf)
            if (n < 0) break
            total += n
            if (total > maxBytes) return null
            out.write(buf, 0, n)
        }
        return out.toString(Charsets.UTF_8.name())
    }

    fun sandboxName(raw: String): String? {
        val name = raw.trim()
        if (name.isEmpty() || name.any { it == '/' || it == '\\' }) return null
        if (".." in name) return null
        if (!name.endsWith(".json", ignoreCase = true)) return null
        return name
    }

    fun parse(json: String): Result {
        val inner = appsInner(json) ?: return Result(0, 0, emptyList())
        val rows = mutableListOf<Pair<String, String>>()
        var skipped = 0
        objects(inner).forEach { chunk ->
            val url = field(chunk, "url")
            if (GithubAdd.ownerRepo(url) == null) {
                skipped += 1
                return@forEach
            }
            rows += field(chunk, "id") to url
        }
        return Result(rows.size, skipped, rows)
    }

    fun persist(
        rows: List<Pair<String, String>>,
        pasted: PastedRepoStore,
        verified: GithubVerifiedStore,
        watched: WatchedRepoStore,
    ): Int {
        var saved = 0
        rows.forEach { (id, url) ->
            val repo = GithubAdd.ownerRepo(url) ?: return@forEach
            if (id.isNotEmpty() && '.' in id) {
                GithubAdd.persistPicked(id, repo, pasted, verified, watched)
            }
            watched.add(repo)
            saved += 1
        }
        return saved
    }

    private fun appsInner(json: String): String? {
        val text = json.trim()
        val start = if (text.startsWith('[')) {
            0
        } else {
            val appsAt = text.indexOf("\"apps\"")
            if (appsAt < 0) return null
            text.indexOf('[', appsAt).takeIf { it >= 0 } ?: return null
        }
        val end = matching(text, start, '[', ']') ?: return null
        return text.substring(start + 1, end).trim().takeIf { it.isNotEmpty() }
    }

    private fun objects(inner: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        while (i < inner.length) {
            val start = inner.indexOf('{', i)
            if (start < 0) break
            val end = matching(inner, start, '{', '}') ?: break
            out += inner.substring(start, end + 1)
            i = end + 1
        }
        return out
    }

    private fun matching(text: String, openAt: Int, open: Char, close: Char): Int? {
        var depth = 0
        var i = openAt
        var inStr = false
        var escape = false
        while (i < text.length) {
            val c = text[i]
            when {
                escape -> escape = false
                inStr && c == '\\' -> escape = true
                c == '"' -> inStr = !inStr
                inStr -> Unit
                c == open -> depth++
                c == close -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        return null
    }

    private fun field(chunk: String, name: String): String =
        Regex(""""$name"\s*:\s*"([^"]+)"""").find(chunk)?.groupValues?.get(1)?.trim().orEmpty()
}
