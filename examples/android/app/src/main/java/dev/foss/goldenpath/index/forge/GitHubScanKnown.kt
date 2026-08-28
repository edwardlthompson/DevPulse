package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.RefreshOutletIds
import dev.foss.goldenpath.inventory.RefreshSkip
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.RemoteReleaseOffer

object GitHubScanKnown {
    fun toOffer(
        packageName: String,
        label: String,
        client: GitHubSearchClient,
        releases: GitHubReleaseClient,
        pause: (Long) -> Unit,
        hint: GithubHint?,
        onVerified: (String) -> Unit,
        searchUnknowns: Boolean = false,
    ): RemoteReleaseOffer {
        val bound = hint?.takeIf { it.ownerRepo.trim().contains('/') }
        if (bound != null) {
            onVerified(bound.ownerRepo.trim())
            RefreshTrace.line("github $packageName list from hint ${bound.ownerRepo.trim()}")
            return GitHubHintRelease.offer(packageName, bound, releases, pause)
        }
        if (RefreshSkip.stopped(RefreshOutletIds.GITHUB)) {
            RefreshTrace.line("github $packageName skip search (stopped)")
            return GitHubScan.unknown()
        }
        if (!searchUnknowns) {
            RefreshTrace.line("github $packageName skip search (no hint)")
            return GitHubScan.unknown()
        }
        val offer = GitHubScan.searchOffer(packageName, label, client, releases, pause)
        if (offer.listed) {
            FdroidGithubHints.ownerRepo(offer.pageUrl)?.let(onVerified)
        }
        return offer
    }
}
