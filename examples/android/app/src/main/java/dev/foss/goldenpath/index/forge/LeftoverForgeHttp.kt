package dev.foss.goldenpath.index.forge

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LeftoverForgeHttp : LeftoverSearchClient {
    override fun page(kind: LeftoverKind, query: String): GitHubSearchPage {
        val q = URLEncoder.encode(query, Charsets.UTF_8.name())
        val url = when (kind) {
            LeftoverKind.GitLabSearch ->
                "https://gitlab.com/api/v4/projects?search=$q&simple=true&per_page=5"
            LeftoverKind.GitLabReleases ->
                "https://gitlab.com/api/v4/projects/${pathSeg(query)}/releases?per_page=5"
            LeftoverKind.CodebergSearch ->
                "https://codeberg.org/api/v1/repos/search?q=$q&limit=5"
            LeftoverKind.CodebergReleases ->
                "https://codeberg.org/api/v1/repos/${pathSeg(query)}/releases?limit=5"
        }
        return get(url)
    }

    private fun pathSeg(raw: String): String =
        raw.split('/').joinToString("%2F") { URLEncoder.encode(it, Charsets.UTF_8.name()).replace("+", "%20") }

    private fun get(url: String): GitHubSearchPage {
        val conn = URL(url).openConnection() as HttpURLConnection
        return try {
            conn.instanceFollowRedirects = true
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", GitHubFetchPolicy.USER_AGENT)
            conn.connectTimeout = GitHubFetchPolicy.CONNECT_TIMEOUT_MS
            conn.readTimeout = GitHubFetchPolicy.READ_TIMEOUT_MS
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            GitHubSearchPage(code, stream?.bufferedReader()?.use { it.readText() }.orEmpty())
        } finally {
            conn.disconnect()
        }
    }
}
