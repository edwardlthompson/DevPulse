package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.apkmirror.ApkMirrorFetchPolicy
import java.net.HttpURLConnection
import java.net.URL

object ListingPageHttp {
    fun get(url: String): String? = runCatching {
        if (!url.startsWith("https://")) return@runCatching null
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent", ua(url))
            val mirror = url.contains("apkmirror", ignoreCase = true)
            conn.connectTimeout = if (mirror) 8_000 else 15_000
            conn.readTimeout = if (mirror) 8_000 else 20_000
            if (conn.responseCode !in 200..299) return@runCatching null
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }.getOrNull()

    internal fun ua(url: String): String =
        if (url.contains("apkmirror", ignoreCase = true)) {
            ApkMirrorFetchPolicy.USER_AGENT
        } else {
            ApkHttpFetcher.USER_AGENT
        }
}
