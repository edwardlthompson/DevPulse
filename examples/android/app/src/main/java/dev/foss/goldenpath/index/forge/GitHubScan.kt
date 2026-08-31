package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.ListingMiss
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource

object GitHubScan {
    private val emptyReleases = GitHubReleaseClient { GitHubSearchPage(200, "[]") }

    fun toOffer(
        packageName: String,
        label: String,
        client: GitHubSearchClient,
        releases: GitHubReleaseClient = (client as? GitHubReleaseClient) ?: emptyReleases,
        pause: (Long) -> Unit = { Thread.sleep(it) },
        hint: GithubHint? = null,
        searchUnknowns: Boolean = false,
        onVerified: (String) -> Unit = {},
    ): RemoteReleaseOffer = GitHubScanKnown.toOffer(
        packageName, label, client, releases, pause, hint, onVerified, searchUnknowns,
    )

    internal fun searchOffer(
        packageName: String,
        label: String,
        client: GitHubSearchClient,
        releases: GitHubReleaseClient,
        pause: (Long) -> Unit,
    ): RemoteReleaseOffer {
        val page = runCatching { search(packageName, label, client, pause) }.getOrElse {
            return fail(packageName, it)
        }
        if (page.statusCode == 403 || page.statusCode == 429 || page.statusCode !in 200..299) {
            ForgeRateLimit.noteGithub(page.statusCode, page.retryAfterSec)
            RefreshTrace.line("github $packageName search http ${page.statusCode} unknown ${page.body.length}B")
            return unknown(if (page.statusCode == 403 || page.statusCode == 429) ListingMiss.Forbidden else ListingMiss.Parse)
        }
        val candidates = GitHubRepoParser.parse(page.body).take(5)
        val outcome = if (candidates.isEmpty()) "missing" else "candidates"
        RefreshTrace.line("github $packageName search http ${page.statusCode} $outcome ${page.body.length}B")
        if (candidates.isEmpty()) {
            return RemoteReleaseOffer(RemoteReleasedSource.Forge, listed = false, known = true)
        }
        return runCatching { verify(packageName, candidates, releases, pause) }.getOrElse {
            fail(packageName, it)
        }
    }

    fun pick(packageName: String, label: String, candidates: List<ForgeCandidate>): ForgeCandidate? =
        ForgeMatcher.rank(packageName, label, candidates)?.candidate

    private fun verify(
        packageName: String,
        candidates: List<ForgeCandidate>,
        releases: GitHubReleaseClient,
        pause: (Long) -> Unit,
    ): RemoteReleaseOffer {
        val verified = mutableListOf<ForgeCandidate>()
        val notesByRepo = HashMap<String, String>()
        val apkByRepo = HashMap<String, String>()
        val versionByRepo = HashMap<String, String>()
        var blocked = false
        for (candidate in candidates) {
            val page = listReleases(candidate.ownerRepo, releases, pause)
            if (page.statusCode == 403 || page.statusCode == 429 || page.statusCode !in 200..299) {
                ForgeRateLimit.noteGithub(page.statusCode, page.retryAfterSec)
                RefreshTrace.line(
                    "github $packageName releases ${candidate.ownerRepo} http ${page.statusCode} unknown ${page.body.length}B",
                )
                blocked = true
                break
            }
            val hit = GitHubReleaseParser.firstWithPackage(packageName, page.body)
                ?: if (candidate.ownerRepo.contains(packageName.substringAfterLast('.'), ignoreCase = true)) {
                    GitHubReleaseParser.firstApk(page.body, packageName = packageName)
                } else null
            if (hit == null) {
                RefreshTrace.line(
                    "github $packageName releases ${candidate.ownerRepo} http ${page.statusCode} missing ${page.body.length}B",
                )
                continue
            }
            RefreshTrace.line(
                "github $packageName releases ${candidate.ownerRepo} http ${page.statusCode} listed ${page.body.length}B",
            )
            hit.notes?.let { notesByRepo[candidate.ownerRepo] = it }
            hit.apkUrl?.let { apkByRepo[candidate.ownerRepo] = it }
            hit.versionName?.let { versionByRepo[candidate.ownerRepo] = it }
            val exact = hit.haystack.contains(packageName, ignoreCase = true)
            verified += candidate.copy(
                packageId = if (exact) packageName else candidate.packageId,
                latestReleaseMs = hit.publishedAtMs ?: candidate.latestReleaseMs,
            )
        }
        val best = pickVerified(packageName, verified)
        if (best != null) {
            GitHubNotes.remember(packageName, notesByRepo[best.ownerRepo])
            GitHubNotes.rememberApk(packageName, apkByRepo[best.ownerRepo])
            return RemoteReleaseOffer(
                source = RemoteReleasedSource.Forge,
                ms = best.latestReleaseMs ?: best.latestCommitMs,
                versionName = versionByRepo[best.ownerRepo],
                pageUrl = ForgeUrl.downloadPage("https://github.com/${best.ownerRepo}"),
            )
        }
        if (blocked) return unknown(ListingMiss.Forbidden)
        return RemoteReleaseOffer(RemoteReleasedSource.Forge, listed = false, known = true)
    }

    private fun pickVerified(packageName: String, verified: List<ForgeCandidate>): ForgeCandidate? =
        verified.firstOrNull { it.packageId.equals(packageName, ignoreCase = true) } ?: verified.firstOrNull()

    private fun search(
        packageName: String,
        label: String,
        client: GitHubSearchClient,
        pause: (Long) -> Unit,
    ): GitHubSearchPage {
        GitHubSearchPace.await(sleepMs = pause)
        return fetch(client.searchRepos(GitHubSearchQuery.repositories(packageName, label)), pause) {
            client.searchRepos(GitHubSearchQuery.repositories(packageName, label))
        }
    }

    internal fun listReleases(
        ownerRepo: String,
        releases: GitHubReleaseClient,
        pause: (Long) -> Unit,
    ): GitHubSearchPage = fetch(releases.listReleases(ownerRepo), pause) { releases.listReleases(ownerRepo) }

    private fun fetch(
        first: GitHubSearchPage,
        pause: (Long) -> Unit,
        retry: () -> GitHubSearchPage,
    ): GitHubSearchPage {
        if (first.statusCode != 403 && first.statusCode != 429) return first
        ForgeBackoff.nextDelayMs(first.statusCode, 1)?.let(pause)
        return retry()
    }

    internal fun fail(packageName: String, error: Throwable): RemoteReleaseOffer {
        RefreshTrace.line("github $packageName fail ${error.javaClass.simpleName}: ${error.message}")
        return unknown()
    }

    internal fun unknown(miss: ListingMiss? = null): RemoteReleaseOffer =
        RemoteReleaseOffer(RemoteReleasedSource.Forge, listed = false, known = false, miss = miss)
}
