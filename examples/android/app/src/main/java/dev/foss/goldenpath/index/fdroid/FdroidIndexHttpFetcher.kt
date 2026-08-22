package dev.foss.goldenpath.index.fdroid

import java.net.HttpURLConnection
import java.net.URL

object FdroidIndexHttpFetcher : FdroidIndexFetcher {
    private const val USER_AGENT = "DevPulse/0.1 (https://github.com/edwardlthompson/DevPulse)"

    override fun fetch(url: String): Result<ByteArray> = runCatching {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.instanceFollowRedirects = true
            conn.connectTimeout = 20_000
            conn.readTimeout = 180_000
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                error("fdroid ${conn.responseCode}")
            }
            if (conn.contentLengthLong == 0L) {
                error("empty body")
            }
            val listed = conn.contentLengthLong
            if (listed > FdroidIndexBudget.MAX_BYTES) {
                error("fdroid index ${listed}B over budget")
            }
            val bytes = conn.inputStream.use { input ->
                FdroidIndexUnpack.readBytes(
                    input,
                    FdroidIndexUnpack.isJarUrl(url),
                    FdroidIndexBudget.MAX_BYTES,
                )
            }
            if (bytes.isEmpty()) error("empty body")
            bytes
        } finally {
            conn.disconnect()
        }
    }
}
