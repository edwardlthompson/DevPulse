package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.aptoide.AptoideScan
import dev.foss.goldenpath.index.forge.GitHubScan
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.play.PlayPageClient
import dev.foss.goldenpath.index.play.PlayScan

object ReleaseRefreshProbes {
    fun play(
        packageName: String,
        client: PlayPageClient,
    ): RemoteReleaseOffer = PlayScan.toOffer(packageName, client)

    fun aptoide(
        packageName: String,
        fetcher: AptoideMetaFetcher,
        nowMs: Long,
    ): RemoteReleaseOffer =
        AptoideScan.toPick(
            AptoideScan.lookupOne(packageName, fetcher, nowMs, force = true),
            packageName,
        )?.offers?.firstOrNull()
            ?: RemoteReleaseOffer(RemoteReleasedSource.Aptoide, listed = false, known = false)

    fun github(
        packageName: String,
        label: String,
        client: GitHubSearchClient,
        hint: GithubHint? = null,
        searchUnknowns: Boolean = false,
        onVerified: (String) -> Unit = {},
    ): RemoteReleaseOffer = GitHubScan.toOffer(
        packageName,
        label,
        client,
        hint = hint,
        searchUnknowns = searchUnknowns,
        onVerified = onVerified,
    )
}
