package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideCachePolicy
import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.aptoide.AptoideScan
import dev.foss.goldenpath.index.forge.ForgeCachePolicy
import dev.foss.goldenpath.index.forge.GitHubScan
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.forge.LeftoverForgeScan
import dev.foss.goldenpath.index.forge.LeftoverHint
import dev.foss.goldenpath.index.forge.LeftoverSearchClient
import dev.foss.goldenpath.index.aurora.AuroraPlayDetails
import dev.foss.goldenpath.index.play.PlayCachePolicy
import dev.foss.goldenpath.index.play.PlayPageClient
import dev.foss.goldenpath.index.play.PlayScan

object ReleaseRefreshProbes {
    fun play(
        packageName: String,
        client: PlayPageClient?,
        nowMs: Long = System.currentTimeMillis(),
        aurora: AuroraPlayDetails? = null,
    ): RemoteReleaseOffer = ProbeCache.fresh(
        packageName, RemoteReleasedSource.Play, nowMs,
        PlayCachePolicy.TTL_MS, PlayCachePolicy.MISS_TTL_MS,
    ) ?: ProbeCache.stamp(PlayScan.toOffer(packageName, client, aurora), nowMs)

    fun aptoide(
        packageName: String,
        fetcher: AptoideMetaFetcher,
        nowMs: Long,
    ): RemoteReleaseOffer = ProbeCache.fresh(
        packageName, RemoteReleasedSource.Aptoide, nowMs,
        AptoideCachePolicy.TTL_MS, AptoideCachePolicy.MISS_TTL_MS,
    )
        ?: ProbeCache.stamp(
            AptoideScan.toPick(
                AptoideScan.lookupOne(packageName, fetcher, nowMs, force = false),
                packageName,
            )?.offers?.firstOrNull()
                ?: RemoteReleaseOffer(RemoteReleasedSource.Aptoide, listed = false, known = false),
            nowMs,
        )

    @Suppress("UNUSED_PARAMETER")
    fun github(
        packageName: String,
        label: String,
        client: GitHubSearchClient,
        hint: GithubHint? = null,
        searchUnknowns: Boolean = false,
        leftover: LeftoverSearchClient? = null,
        leftoverHint: LeftoverHint? = null,
        onVerified: (String) -> Unit = {},
        nowMs: Long = System.currentTimeMillis(),
    ): RemoteReleaseOffer {
        val cached = ProbeCache.fresh(
            packageName, RemoteReleasedSource.Forge, nowMs,
            ForgeCachePolicy.TTL_MS, ForgeCachePolicy.MISS_TTL_MS,
        )
        if (hint != null) {
            return ProbeCache.stamp(
                GitHubScan.toOffer(packageName, label, client, hint = hint, onVerified = onVerified),
                nowMs,
            )
        }
        cached?.takeIf { it.listed }?.let { return it }
        leftoverHint?.let { return ProbeCache.stamp(LeftoverForgeScan.fromHint(packageName, it), nowMs) }
        cached?.let { return it }
        if (storeSettled(packageName)) {
            RefreshTrace.line("github $packageName skip search (listed)")
            return ProbeCache.stamp(
                RemoteReleaseOffer(RemoteReleasedSource.Forge, listed = false, known = true),
                nowMs,
            )
        }
        val offer = GitHubScan.toOffer(
            packageName, label, client, hint = null, searchUnknowns = true, onVerified = onVerified,
        )
        return ProbeCache.stamp(offer, nowMs)
    }

    internal fun storeSettled(packageName: String): Boolean {
        val offers = RemoteReleaseMemory.byPackage[packageName]?.offers.orEmpty()
        if (offers.any { it.source == RemoteReleasedSource.Play && it.known }) return true
        return offers.any { it.listed && it.source != RemoteReleasedSource.Forge && it.source != RemoteReleasedSource.None }
    }
}
