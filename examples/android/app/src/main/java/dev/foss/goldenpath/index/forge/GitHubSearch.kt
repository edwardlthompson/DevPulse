package dev.foss.goldenpath.index.forge

object GitHubFetchPolicy {
    const val MIN_INTERVAL_MS = 3_000L
    const val CONNECT_TIMEOUT_MS = 10_000
    const val READ_TIMEOUT_MS = 15_000
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
