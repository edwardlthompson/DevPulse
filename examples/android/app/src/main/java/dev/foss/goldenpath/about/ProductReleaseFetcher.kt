package dev.foss.goldenpath.about

import dev.foss.goldenpath.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ProductReleaseFetcher {
    data class Parsed(val htmlUrl: String, val assets: List<ProductUpdate.NamedAsset>, val body: String? = null)

    suspend fun fetchLatest(): Parsed? = withContext(Dispatchers.IO) {
        val conn = URL(ProductUpdate.RELEASES_API).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty(
                "User-Agent",
                "DevPulse/${BuildConfig.VERSION_NAME} (https://github.com/${ProductUpdate.RELEASE_REPO})",
            )
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            parse(conn.inputStream.bufferedReader().use { it.readText() })
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    fun parse(json: String): Parsed? {
        return try {
            val root = JSONObject(json)
            val htmlUrl = root.optString("html_url", ProductUpdate.RELEASES_PAGE)
            val body = root.optString("body").trim().ifEmpty { null }
            val assets = mutableListOf<ProductUpdate.NamedAsset>()
            val arr = root.optJSONArray("assets") ?: return Parsed(htmlUrl, assets, body)
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val name = item.optString("name")
                val url = item.optString("browser_download_url")
                if (name.isNotBlank() && url.isNotBlank()) {
                    assets.add(ProductUpdate.NamedAsset(name, url))
                }
            }
            Parsed(htmlUrl, assets, body)
        } catch (_: Exception) {
            null
        }
    }
}
