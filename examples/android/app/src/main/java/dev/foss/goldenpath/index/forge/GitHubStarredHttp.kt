package dev.foss.goldenpath.index.forge

import java.net.HttpURLConnection
import java.net.URL

class GitHubStarredHttp(private val token: String?) {
    fun page(page: Int): GitHubSearchPage {
        val n = page.coerceAtLeast(1)
        return get("https://api.github.com/user/starred?per_page=100&page=$n")
    }

    private fun get(url: String): GitHubSearchPage {
        val conn = URL(url).openConnection() as HttpURLConnection
        return try {
            conn.instanceFollowRedirects = true
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", GitHubFetchPolicy.USER_AGENT)
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            token?.trim()?.takeIf { it.isNotEmpty() }?.let {
                conn.setRequestProperty("Authorization", "Bearer $it")
            }
            conn.connectTimeout = GitHubFetchPolicy.CONNECT_TIMEOUT_MS
            conn.readTimeout = GitHubFetchPolicy.READ_TIMEOUT_MS
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            GitHubSearchPage(code, body, RetryAfter.seconds(conn.getHeaderField("Retry-After")))
        } finally {
            conn.disconnect()
        }
    }
}
