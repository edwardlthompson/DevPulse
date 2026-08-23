package dev.foss.goldenpath.index.play

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import dev.foss.goldenpath.index.forge.RetryAfter
import java.net.URL
import java.net.URLEncoder

object PlayHttpFetcher : PlayPageClient {
    override fun get(packageName: String): PlayPageResponse {
        val encoded = URLEncoder.encode(packageName, Charsets.UTF_8.name())
        val conn = URL("https://play.google.com/store/apps/details?id=$encoded&hl=en&gl=US")
            .openConnection() as HttpURLConnection
        try {
            conn.instanceFollowRedirects = true
            conn.useCaches = false
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", PlayFetchPolicy.USER_AGENT)
            conn.setRequestProperty("Accept-Language", PlayFetchPolicy.ACCEPT_LANGUAGE)
            conn.setRequestProperty("Accept", PlayFetchPolicy.ACCEPT)
            conn.connectTimeout = PlayFetchPolicy.CONNECT_TIMEOUT_MS
            conn.readTimeout = PlayFetchPolicy.READ_TIMEOUT_MS
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.let { readListed(it, PlayFetchPolicy.MAX_BODY_BYTES) }.orEmpty()
            return PlayPageResponse(code, body, RetryAfter.seconds(conn.getHeaderField("Retry-After")))
        } finally {
            conn.disconnect()
        }
    }

    internal fun readListed(input: InputStream, maxBytes: Int): String {
        val out = ByteArrayOutputStream(64 * 1024)
        val buf = ByteArray(16_384)
        var total = 0
        while (total < maxBytes) {
            val n = input.read(buf, 0, minOf(buf.size, maxBytes - total))
            if (n < 0) break
            out.write(buf, 0, n)
            total += n
            if (total >= 900_000) {
                val soFar = out.toString(Charsets.UTF_8.name())
                if (PlayHtmlParser.looksListed(soFar)) return soFar
            }
        }
        return out.toString(Charsets.UTF_8.name())
    }
}
