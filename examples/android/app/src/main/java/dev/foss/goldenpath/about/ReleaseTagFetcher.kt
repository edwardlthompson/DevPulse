package dev.foss.goldenpath.about

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class LatestRelease(
    val tag: String?,
    val assets: List<ReleaseAsset>,
)

object ReleaseTagFetcher {
    fun loadReleaseRepo(context: Context): String? {
        return try {
            val json = context.assets.open("app-update.json").bufferedReader().use { it.readText() }
            val repo = JSONObject(json).optString("release_repo", "").trim()
            when {
                repo.isEmpty() -> null
                repo.equals("OWNER/REPO", ignoreCase = true) -> null
                else -> repo
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun fetchLatestRelease(releaseRepo: String): LatestRelease? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/$releaseRepo/releases/latest")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val tag = json.optString("tag_name", "").ifEmpty { null }
            val assets = mutableListOf<ReleaseAsset>()
            val arr = json.optJSONArray("assets")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    val name = item.optString("name", "")
                    val format = name.substringAfterLast('.', "bin")
                    assets.add(
                        ReleaseAsset(
                            format = format,
                            url = item.optString("browser_download_url", ""),
                        ),
                    )
                }
            }
            LatestRelease(tag, assets)
        } catch (_: Exception) {
            null
        }
    }
}
