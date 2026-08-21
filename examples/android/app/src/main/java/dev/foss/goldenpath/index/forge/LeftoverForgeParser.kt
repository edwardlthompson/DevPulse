package dev.foss.goldenpath.index.forge

fun interface LeftoverSearchClient {
    fun page(kind: LeftoverKind, query: String): GitHubSearchPage
}

enum class LeftoverKind { GitLabSearch, GitLabReleases, CodebergSearch, CodebergReleases }

object LeftoverForgeParser {
    private val gitlabPath = Regex(""""path_with_namespace"\s*:\s*"([^"]+)"""")
    private val gitlabName = Regex(""""name"\s*:\s*"([^"]+)"""")
    private val gitlabDesc = Regex(""""description"\s*:\s*"([^"]*)"""")
    private val gitlabActivity = Regex(""""last_activity_at"\s*:\s*"([^"]+)"""")
    private val codebergName = Regex(""""full_name"\s*:\s*"([^"]+)"""")
    private val releasedAt = Regex(""""(?:released_at|published_at)"\s*:\s*"([^"]+)"""")

    fun gitlabProjects(json: String): List<ForgeCandidate> =
        gitlabPath.findAll(json).map { match ->
            val repo = match.groupValues[1]
            val window = json.substring(match.range.first).take(2_000)
            ForgeCandidate(
                host = ForgeHost.GitLab,
                ownerRepo = repo,
                packageId = null,
                title = gitlabName.find(window)?.groupValues?.get(1) ?: repo.substringAfter('/'),
                latestCommitMs = GitHubRepoParser.isoMs(gitlabActivity.find(window)?.groupValues?.get(1)),
                latestReleaseMs = null,
                archived = false,
                description = gitlabDesc.find(window)?.groupValues?.get(1)?.ifEmpty { null },
            )
        }.toList()

    fun codebergRepos(json: String): List<ForgeCandidate> {
        val items = json.substringAfter("\"data\"", json)
        return codebergName.findAll(items).map { match ->
            val repo = match.groupValues[1]
            val window = items.substring(match.range.first).take(2_000)
            ForgeCandidate(
                host = ForgeHost.Codeberg,
                ownerRepo = repo,
                packageId = null,
                title = repo.substringAfter('/'),
                latestCommitMs = GitHubRepoParser.isoMs(gitlabActivity.find(window)?.groupValues?.get(1)),
                latestReleaseMs = null,
                archived = false,
                description = gitlabDesc.find(window)?.groupValues?.get(1)?.ifEmpty { null },
            )
        }.toList()
    }

    fun firstReleaseMs(json: String): Long? =
        GitHubRepoParser.isoMs(releasedAt.find(json)?.groupValues?.get(1))

    fun releaseHaystack(json: String): String = json.take(8_000)
}
