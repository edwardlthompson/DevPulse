package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.InstalledApp

data class StarredBindResult(
    val stars: Int,
    val matched: Int,
    val statusCode: Int,
)

object GitHubStarredScan {
    const val MAX_PAGES = 5

    fun bind(
        pages: List<GitHubSearchPage>,
        installed: List<InstalledApp>,
        library: Map<String, String>,
    ): Pair<List<LibraryMatch>, StarredBindResult> {
        if (pages.isEmpty()) {
            return emptyList<LibraryMatch>() to StarredBindResult(0, 0, 408)
        }
        val blocked = pages.firstOrNull { it.statusCode == 403 || it.statusCode == 429 }
        if (blocked != null) {
            return emptyList<LibraryMatch>() to StarredBindResult(0, 0, blocked.statusCode)
        }
        val failed = pages.firstOrNull { it.statusCode !in 200..299 }
        if (failed != null && pages.none { it.statusCode in 200..299 }) {
            return emptyList<LibraryMatch>() to StarredBindResult(0, 0, failed.statusCode)
        }
        val repos = pages.filter { it.statusCode in 200..299 }
            .flatMap { GitHubStarredParser.ownerRepos(it.body) }
            .distinct()
        val matches = repos.flatMap { repo ->
            ForgeLibraryMatch.autoBind(ForgeLibraryMatch.bind(repo, installed, library))
        }
        val code = failed?.statusCode ?: 200
        return matches to StarredBindResult(repos.size, matches.size, code)
    }
}
