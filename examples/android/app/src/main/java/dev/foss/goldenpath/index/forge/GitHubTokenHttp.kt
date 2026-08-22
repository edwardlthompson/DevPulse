package dev.foss.goldenpath.index.forge

import java.net.HttpURLConnection
import java.net.URL

object GitHubTokenHttp : GitHubTokenClient {
    const val RATE_URL = "https://api.github.com/rate_limit"

    override fun check(token: String): Result<GitHubSearchPage> = runCatching {
        val conn = URL(RATE_URL).openConnection() as HttpURLConnection
        try {
            conn.instanceFollowRedirects = true
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", GitHubFetchPolicy.USER_AGENT)
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("Authorization", "Bearer ${token.trim()}")
            conn.connectTimeout = GitHubFetchPolicy.CONNECT_TIMEOUT_MS
            conn.readTimeout = GitHubFetchPolicy.READ_TIMEOUT_MS
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            GitHubSearchPage(code, body)
        } finally {
            conn.disconnect()
        }
    }
}
