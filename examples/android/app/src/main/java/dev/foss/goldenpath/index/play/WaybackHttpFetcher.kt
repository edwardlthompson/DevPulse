package dev.foss.goldenpath.index.play

import java.net.HttpURLConnection
import java.net.URL

object WaybackHttpFetcher : WaybackPlayClient {
    const val CONNECT_TIMEOUT_MS = 8_000
    const val READ_TIMEOUT_MS = 12_000
    const val MAX_BYTES = 1_250_000

    override fun recover(packageName: String): PlayLookup? = runCatching {
        val json = get(WaybackSnapshot.availabilityUrl(packageName)) ?: return null
        val snap = WaybackSnapshot.snapshotUrl(json) ?: return null
        val html = get(snap) ?: return null
        val lookup = PlayHtmlParser.parse(html)
        lookup.takeIf { it.updatedOnMs != null }
    }.getOrNull()

    internal fun get(url: String): String? {
        val conn = URL(url).openConnection() as HttpURLConnection
        return try {
            conn.instanceFollowRedirects = true
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            conn.setRequestProperty("User-Agent", PlayFetchPolicy.USER_AGENT)
            conn.setRequestProperty("Accept", PlayFetchPolicy.ACCEPT)
            if (conn.responseCode !in 200..299) return null
            conn.inputStream.use { PlayHttpFetcher.readListed(it, MAX_BYTES) }
        } finally {
            conn.disconnect()
        }
    }
}
