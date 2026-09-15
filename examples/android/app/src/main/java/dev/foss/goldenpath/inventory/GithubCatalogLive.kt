package dev.foss.goldenpath.inventory

import android.content.Context
import android.content.SharedPreferences
import dev.foss.goldenpath.index.forge.FileGithubVerifiedStore
import dev.foss.goldenpath.index.forge.GithubCatalogFetcher
import dev.foss.goldenpath.index.forge.GithubCatalogPage
import dev.foss.goldenpath.index.forge.GithubCatalogSync
import dev.foss.goldenpath.index.forge.GithubShippedCatalog
import dev.foss.goldenpath.index.forge.GithubVerifiedStore
import dev.foss.goldenpath.index.forge.GitHubFetchPolicy
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object GithubCatalogLive {
    private const val PREFS = "github_catalog"
    private const val LAST_PULL = "last_pull_at"
    private const val ETAG = "etag"

    fun loadShipped(context: Context): Map<String, String> =
        runCatching { GithubShippedCatalog.fromAssets(context.assets) }.getOrDefault(emptyMap())

    fun pullIfDue(
        context: Context,
        store: GithubVerifiedStore = FileGithubVerifiedStore(File(context.filesDir, "github_verified.tsv")),
        nowMs: Long = System.currentTimeMillis(),
        fetch: GithubCatalogFetcher = GithubCatalogFetcher { url, etag -> get(url, etag) },
    ) {
        val prefs = prefs(context)
        val last = prefs.getLong(LAST_PULL, -1L).takeIf { it >= 0L }
        if (!GithubCatalogSync.due(last, nowMs)) return
        val etag = prefs.getString(ETAG, null)
        val page = runCatching { fetch.get(GithubCatalogSync.rawUrl(), etag) }.getOrNull() ?: return
        if (page.status == 404 || page.status == 403 || page.status == 429) {
            prefs.edit().putLong(LAST_PULL, nowMs).apply()
            return
        }
        if (page.status !in 200..299 && page.status != 304) return
        val applied = GithubCatalogSync.apply(page, store.load(), etag)
        if (applied.saved) store.save(applied.rows)
        prefs.edit()
            .putLong(LAST_PULL, nowMs)
            .putString(ETAG, applied.etag)
            .apply()
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun get(url: String, etag: String?): GithubCatalogPage {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.instanceFollowRedirects = true
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", GitHubFetchPolicy.USER_AGENT)
            conn.setRequestProperty("Accept", "text/plain")
            if (!etag.isNullOrBlank()) conn.setRequestProperty("If-None-Match", etag)
            conn.connectTimeout = GitHubFetchPolicy.CONNECT_TIMEOUT_MS
            conn.readTimeout = GitHubFetchPolicy.READ_TIMEOUT_MS
            val code = conn.responseCode
            val body = if (code == 304) {
                ""
            } else {
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            return GithubCatalogPage(code, body, conn.getHeaderField("ETag"))
        } finally {
            conn.disconnect()
        }
    }
}
