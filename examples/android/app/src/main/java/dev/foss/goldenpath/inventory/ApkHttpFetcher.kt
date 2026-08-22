package dev.foss.goldenpath.inventory

import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ApkHttpFetcher : ApkBytesFetcher {
    const val CONNECT_TIMEOUT_MS = 15_000
    const val READ_TIMEOUT_MS = 60_000
    const val MAX_BYTES = 200 * 1024 * 1024
    const val USER_AGENT = "DevPulse/0.23 (https://github.com/edwardlthompson/DevPulse)"

    override fun get(url: String): Result<ByteArray> = get(url, null)

    fun get(
        url: String,
        onProgress: ((Long, Long) -> Unit)?,
        userAgent: String = USER_AGENT,
    ): Result<ByteArray> = runCatching {
        val safe = ApkDownloadUrl.httpsFile(url) ?: error("url")
        val conn = URL(safe).openConnection() as HttpURLConnection
        try {
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent", userAgent)
            if (safe.contains("apkpure", true) || safe.contains("cdnpure", true)) {
                conn.setRequestProperty("Referer", "https://apkpure.com/")
            } else if (safe.contains("apkmirror", true)) {
                conn.setRequestProperty("Referer", "https://www.apkmirror.com/")
            }
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            if (conn.responseCode !in 200..299) error("apk ${conn.responseCode}")
            val total = conn.contentLengthLong
            val out = ByteArrayOutputStream()
            val buf = ByteArray(16 * 1024)
            var read = 0L
            conn.inputStream.use { input ->
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    out.write(buf, 0, n)
                    read += n
                    if (read > MAX_BYTES) error("apk too large")
                    onProgress?.invoke(read, total)
                }
            }
            out.toByteArray()
        } finally {
            conn.disconnect()
        }
    }
}
