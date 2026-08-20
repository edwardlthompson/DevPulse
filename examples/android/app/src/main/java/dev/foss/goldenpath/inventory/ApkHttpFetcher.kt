package dev.foss.goldenpath.inventory

import java.net.HttpURLConnection
import java.net.URL

object ApkHttpFetcher : ApkBytesFetcher {
    const val CONNECT_TIMEOUT_MS = 15_000
    const val READ_TIMEOUT_MS = 60_000
    const val MAX_BYTES = 200 * 1024 * 1024
    const val USER_AGENT = "DevPulse/0.23 (https://github.com/edwardlthompson/DevPulse)"

    override fun get(url: String): Result<ByteArray> = runCatching {
        val safe = ApkDownloadUrl.httpsFile(url) ?: error("url")
        val conn = URL(safe).openConnection() as HttpURLConnection
        try {
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent", USER_AGENT)
            if (safe.contains("apkpure", ignoreCase = true)) {
                conn.setRequestProperty("Referer", "https://apkpure.com/")
            }
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            if (conn.responseCode !in 200..299) error("apk ${conn.responseCode}")
            val bytes = conn.inputStream.use { it.readBytes() }
            if (bytes.size > MAX_BYTES) error("apk too large")
            bytes
        } finally {
            conn.disconnect()
        }
    }
}
