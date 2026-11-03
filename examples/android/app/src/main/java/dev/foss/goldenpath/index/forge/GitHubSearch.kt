package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.index.SourceFetchPolicy

object GitHubFetchPolicy {
    const val MIN_INTERVAL_MS = 3_000L
    const val CONNECT_TIMEOUT_MS = SourceFetchPolicy.CONNECT_TIMEOUT_MS
    const val READ_TIMEOUT_MS = SourceFetchPolicy.READ_TIMEOUT_MS
    const val USER_AGENT = "DevPulse/0.1 (https://github.com/edwardlthompson/DevPulse)"
    const val SEARCH_URL = "https://api.github.com/search/repositories"
    const val REPOS_URL = "https://api.github.com/repos"
}

data class GitHubSearchPage(
    val statusCode: Int,
    val body: String,
    val retryAfterSec: Long? = null,
)

fun interface GitHubSearchClient {
    fun searchRepos(query: String): GitHubSearchPage
}

fun interface GitHubReleaseClient {
    fun listReleases(ownerRepo: String): GitHubSearchPage
}

object GitHubSearchQuery {
    fun repositories(packageName: String, label: String): String {
        val pkg = packageName.trim()
        val name = label.trim()
        val pkgQ = pkg.takeIf { it.isNotEmpty() }?.let { "\"$it\"" }
        val nameQ = name.takeIf { it.isNotEmpty() && !it.equals(pkg, ignoreCase = true) }?.let { raw ->
            if (raw.any { it.isWhitespace() }) "\"$raw\"" else raw
        }
        return listOfNotNull(pkgQ, nameQ).joinToString(" OR ")
    }
}
