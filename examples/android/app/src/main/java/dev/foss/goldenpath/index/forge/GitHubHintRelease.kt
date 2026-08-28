package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.RefreshOutletIds
import dev.foss.goldenpath.inventory.RefreshSkip
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource

object GitHubHintRelease {
    fun offer(
        packageName: String,
        hint: GithubHint,
        releases: GitHubReleaseClient,
        pause: (Long) -> Unit,
        opt: GithubAppOpt? = null,
    ): RemoteReleaseOffer {
        val ownerRepo = hint.ownerRepo.trim()
        val fallback = RemoteReleaseOffer(
            source = RemoteReleasedSource.Forge,
            ms = hint.ms,
            versionName = hint.versionName,
            pageUrl = ForgeUrl.downloadPage("https://github.com/$ownerRepo"),
        )
        if (RefreshSkip.stopped(RefreshOutletIds.GITHUB)) return fallback
        val page = runCatching { GitHubScan.listReleases(ownerRepo, releases, pause) }.getOrElse {
            RefreshTrace.line("github $packageName hint releases fail ${it.javaClass.simpleName}")
            return fallback
        }
        if (page.statusCode !in 200..299) {
            ForgeRateLimit.noteGithub(page.statusCode, page.retryAfterSec)
            RefreshTrace.line(
                "github $packageName hint releases http ${page.statusCode} fallback ${page.body.length}B",
            )
            return fallback
        }
        val hit = GitHubReleasePick.bound(
            packageName,
            page.body,
            includePrereleases = opt?.includePrereleases ?: true,
            apkRegex = opt?.apkRegex,
        ) ?: return fallback.also {
            RefreshTrace.line("github $packageName hint releases missing ${page.body.length}B")
        }
        GitHubNotes.remember(packageName, hit.notes)
        GitHubNotes.rememberApk(packageName, hit.apkUrl, hit.versionName)
        RefreshTrace.line("github $packageName hint releases listed ${page.body.length}B")
        return RemoteReleaseOffer(
            source = RemoteReleasedSource.Forge,
            ms = hit.publishedAtMs ?: hint.ms,
            versionName = hit.versionName ?: hint.versionName,
            pageUrl = fallback.pageUrl,
        )
    }
}
