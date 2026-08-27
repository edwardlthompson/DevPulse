package dev.foss.goldenpath.index.fdroid

import java.net.URI

object FdroidCustomIndex {
    fun valid(raw: String): Boolean {
        val url = raw.trim()
        if (!url.startsWith("https://", ignoreCase = true)) return false
        return runCatching {
            val uri = URI(url)
            val host = uri.host.orEmpty().lowercase()
            if (host.isEmpty() || host == "localhost" || host.endsWith(".local")) return false
            if (host.all { it.isDigit() || it == '.' || it == ':' }) return false
            val path = uri.path.orEmpty().lowercase()
            path.endsWith("index-v1.jar") ||
                path.endsWith("index-v1.json") ||
                path.endsWith("index-v2.json") ||
                path.endsWith(".jar")
        }.getOrDefault(false)
    }
}
