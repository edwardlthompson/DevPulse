package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.ListingMiss
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource

internal object GitHubScanVerify {
    fun releases(
        packageName: String,
        label: String,
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
            val page = GitHubScan.listReleases(candidate.ownerRepo, releases, pause)
            if (page.statusCode == 403 || page.statusCode == 429 || page.statusCode !in 200..299) {
                ForgeRateLimit.noteGithub(page.statusCode, page.retryAfterSec)
                RefreshTrace.line(
                    "github $packageName releases ${candidate.ownerRepo} http ${page.statusCode} unknown ${page.body.length}B",
                )
                blocked = true
                break
            }
            val hit = GitHubReleasePick.leftover(packageName, label, candidate.ownerRepo, page.body)
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
        val best = pick(packageName, verified)
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
        if (blocked) return GitHubScan.unknown(ListingMiss.Forbidden)
        return RemoteReleaseOffer(
            RemoteReleasedSource.Forge,
            listed = false,
            known = true,
            miss = ListingMiss.Searched,
        )
    }

    private fun pick(packageName: String, verified: List<ForgeCandidate>): ForgeCandidate? {
        verified.firstOrNull { it.packageId.equals(packageName, ignoreCase = true) }?.let { return it }
        return verified.maxByOrNull { it.latestReleaseMs ?: it.latestCommitMs ?: 0L }
    }
}
