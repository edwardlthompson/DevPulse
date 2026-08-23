package dev.foss.goldenpath.index.aptoide

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object AptoideHttpFetcher : AptoideMetaFetcher {
    override fun fetch(packageName: String): Result<String> = runCatching {
        val encoded = URLEncoder.encode(packageName, Charsets.UTF_8.name())
        val url = URL(
            "${AptoideFetchPolicy.META_URL}?package_name=$encoded&store_name=${AptoideCatalog.storeName()}",
        )
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", AptoideFetchPolicy.USER_AGENT)
            conn.connectTimeout = AptoideFetchPolicy.CONNECT_TIMEOUT_MS
            conn.readTimeout = AptoideFetchPolicy.READ_TIMEOUT_MS
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                error("aptoide ${conn.responseCode}")
            }
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
