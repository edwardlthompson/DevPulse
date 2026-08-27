package dev.foss.goldenpath.inventory

import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object ApkSizeCap {
    fun allow(contentLength: Long, maxBytes: Long = ApkHttpFetcher.MAX_BYTES): Boolean =
        contentLength <= 0L || contentLength <= maxBytes
}

object ApkHttpFetcher : ApkBytesFetcher {
    const val CONNECT_TIMEOUT_MS = 15_000
    const val READ_TIMEOUT_MS = 60_000
    const val MAX_BYTES = 200L * 1024 * 1024
    const val USER_AGENT = "DevPulse/0.23 (https://github.com/edwardlthompson/DevPulse)"

    override fun get(url: String): Result<ByteArray> = get(url, null)

    fun get(
        url: String,
        onProgress: ((Long, Long) -> Unit)?,
        userAgent: String = USER_AGENT,
    ): Result<ByteArray> = open(url, userAgent) { conn, total ->
        val out = ByteArrayOutputStream()
        copy(conn, total, onProgress) { buf, n -> out.write(buf, 0, n) }
        out.toByteArray()
    }

    fun toFile(
        url: String,
        dest: File,
        onProgress: ((Long, Long) -> Unit)?,
        userAgent: String = USER_AGENT,
    ): Result<File> = open(url, userAgent) { conn, total ->
        dest.parentFile?.mkdirs()
        dest.outputStream().use { out ->
            copy(conn, total, onProgress) { buf, n -> out.write(buf, 0, n) }
        }
        dest
    }.onFailure { dest.delete() }

    private fun <T> open(
        url: String,
        userAgent: String,
        read: (HttpURLConnection, Long) -> T,
    ): Result<T> = runCatching {
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
            if (!ApkSizeCap.allow(total)) error("apk too large")
            read(conn, total)
        } finally {
            conn.disconnect()
        }
    }

    private fun copy(
        conn: HttpURLConnection,
        total: Long,
        onProgress: ((Long, Long) -> Unit)?,
        write: (ByteArray, Int) -> Unit,
    ) {
        val buf = ByteArray(16 * 1024)
        var read = 0L
        conn.inputStream.use { input ->
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                if (read + n > MAX_BYTES) error("apk too large")
                write(buf, n)
                read += n
                onProgress?.invoke(read, total)
            }
        }
    }
}
