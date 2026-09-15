package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.forge.GitHubLeftoverNoise
import dev.foss.goldenpath.index.forge.GitHubScan
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.forge.GithubSearchSkip
import dev.foss.goldenpath.index.forge.GithubVerifiedStore
import dev.foss.goldenpath.index.forge.PackageIdAliases

data class GitHubDiscoverTick(
    val searched: Int,
    val hits: Int,
    val misses: Int,
    val skip: Map<String, Long>,
    val remaining: Int,
)

object GitHubDiscover {
    const val BATCH = 5

    fun queued(
        apps: List<InstalledApp>,
        library: Map<String, GithubHint>,
        skip: Map<String, Long>,
        nowMs: Long,
        listed: (String) -> Boolean,
    ): List<InstalledApp> = apps.filter { app ->
        val pkg = app.packageName
        !app.isSystemApp &&
            PackageIdAliases.hint(pkg, library) == null &&
            !GitHubLeftoverNoise.skip(pkg) &&
            !GithubSearchSkip.blocked(pkg, skip, nowMs) &&
            !listed(pkg)
    }

    fun tick(
        apps: List<InstalledApp>,
        library: Map<String, GithubHint>,
        skip: Map<String, Long>,
        client: GitHubSearchClient,
        verified: GithubVerifiedStore,
        nowMs: Long,
        listed: (String) -> Boolean,
        pause: (Long) -> Unit = {},
    ): GitHubDiscoverTick {
        val batch = queued(apps, library, skip, nowMs, listed).take(BATCH)
        var hits = 0
        var misses = 0
        var nextSkip = skip
        batch.forEach { app ->
            val offer = GitHubScan.toOffer(
                app.packageName,
                app.label,
                client,
                pause = pause,
                searchUnknowns = true,
                onVerified = { ownerRepo -> verified.put(app.packageName, ownerRepo) },
            )
            if (offer.listed) {
                hits += 1
            } else if (offer.known && offer.miss == ListingMiss.Searched) {
                misses += 1
                nextSkip = GithubSearchSkip.remember(nextSkip, app.packageName, nowMs)
            }
        }
        val remaining = queued(
            apps,
            library + verified.load().mapValues { GithubHint(it.value) },
            nextSkip,
            nowMs,
            listed,
        ).size
        return GitHubDiscoverTick(batch.size, hits, misses, nextSkip, remaining)
    }
}
