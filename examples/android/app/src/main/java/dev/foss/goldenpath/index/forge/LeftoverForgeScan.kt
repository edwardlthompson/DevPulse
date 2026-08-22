package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.RefreshOutletIds
import dev.foss.goldenpath.inventory.RefreshSkip
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource

object LeftoverForgeScan {
    fun fromHint(packageName: String, hint: LeftoverHint): RemoteReleaseOffer {
        val href = when (hint.host) {
            ForgeHost.GitLab -> "https://gitlab.com/${hint.ownerRepo}"
            ForgeHost.Codeberg -> "https://codeberg.org/${hint.ownerRepo}"
            else -> null
        }
        RefreshTrace.line("${hint.host.name.lowercase()} $packageName list from hint ${hint.ownerRepo}")
        return RemoteReleaseOffer(
            source = RemoteReleasedSource.Forge,
            ms = hint.ms,
            versionName = hint.versionName,
            pageUrl = ForgeUrl.downloadPage(href),
        )
    }

    fun first(
        packageName: String,
        label: String,
        client: LeftoverSearchClient,
        query: String = GitHubSearchQuery.repositories(packageName, label),
    ): RemoteReleaseOffer? {
        if (query.isBlank() || ForgeRateLimit.leftoverBlocked()) return null
        if (RefreshSkip.stopped(RefreshOutletIds.LEFTOVER)) return null
        val gitlab = hostOffer(packageName, query, client, ForgeHost.GitLab, LeftoverKind.GitLabSearch, LeftoverKind.GitLabReleases)
        if (gitlab?.listed == true) return gitlab
        val codeberg = hostOffer(packageName, query, client, ForgeHost.Codeberg, LeftoverKind.CodebergSearch, LeftoverKind.CodebergReleases)
        if (codeberg?.listed == true) return codeberg
        if (gitlab?.known == true && codeberg?.known == true) {
            return RemoteReleaseOffer(RemoteReleasedSource.Forge, listed = false, known = true)
        }
        return null
    }

    private fun hostOffer(
        packageName: String,
        query: String,
        client: LeftoverSearchClient,
        host: ForgeHost,
        searchKind: LeftoverKind,
        releaseKind: LeftoverKind,
    ): RemoteReleaseOffer? {
        if (host == ForgeHost.GitLab && ForgeRateLimit.leftoverBlocked()) return null
        val page = runCatching { client.page(searchKind, query) }.getOrElse {
            RefreshTrace.line("${host.name.lowercase()} $packageName fail ${it.javaClass.simpleName}")
            if (host == ForgeHost.GitLab) ForgeRateLimit.noteLeftoverTimeout()
            return null
        }
        if (page.statusCode !in 200..299) return null
        val candidates = when (host) {
            ForgeHost.GitLab -> LeftoverForgeParser.gitlabProjects(page.body)
            ForgeHost.Codeberg -> LeftoverForgeParser.codebergRepos(page.body)
            else -> emptyList()
        }.take(5)
        val pick = candidates.firstOrNull { candidate ->
            val releases = runCatching { client.page(releaseKind, candidate.ownerRepo) }.getOrNull() ?: return@firstOrNull false
            releases.statusCode in 200..299 &&
                ForgePackageEvidence.inText(packageName, LeftoverForgeParser.releaseHaystack(releases.body))
        } ?: return RemoteReleaseOffer(RemoteReleasedSource.Forge, listed = false, known = true)
        val releases = client.page(releaseKind, pick.ownerRepo)
        val ms = LeftoverForgeParser.firstReleaseMs(releases.body) ?: pick.latestCommitMs
        RefreshTrace.line("${host.name.lowercase()} $packageName leftover ${pick.ownerRepo}")
        return RemoteReleaseOffer(
            source = RemoteReleasedSource.Forge,
            ms = ms,
            pageUrl = ForgeUrl.downloadPage(
                when (host) {
                    ForgeHost.GitLab -> "https://gitlab.com/${pick.ownerRepo}"
                    ForgeHost.Codeberg -> "https://codeberg.org/${pick.ownerRepo}"
                    else -> null
                },
            ),
        )
    }
}
