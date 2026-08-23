package dev.foss.goldenpath.index.forge

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class GitHubSearchHttp(private val token: String? = null) : GitHubSearchClient, GitHubReleaseClient {
    override fun searchRepos(query: String): GitHubSearchPage {
        val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
        return get("${GitHubFetchPolicy.SEARCH_URL}?q=$encoded&per_page=5")
    }

    override fun listReleases(ownerRepo: String): GitHubSearchPage {
        val slash = ownerRepo.indexOf('/')
        if (slash <= 0 || slash == ownerRepo.lastIndex) return GitHubSearchPage(200, "[]")
        val owner = pathSeg(ownerRepo.substring(0, slash))
        val repo = pathSeg(ownerRepo.substring(slash + 1))
        return get("${GitHubFetchPolicy.REPOS_URL}/$owner/$repo/releases?per_page=5")
    }

    private fun pathSeg(raw: String): String =
        URLEncoder.encode(raw, Charsets.UTF_8.name()).replace("+", "%20")

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
