package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import dev.foss.goldenpath.index.fdroid.FdroidHostResolver
import dev.foss.goldenpath.index.fdroid.FdroidIndexBudget
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexParser
import dev.foss.goldenpath.index.fdroid.FdroidIndexStore
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.forge.FdroidGithubHints
import dev.foss.goldenpath.index.forge.GithubHint
import java.util.concurrent.ExecutorService

data class FdroidRepoLoad(
    val repoId: String,
    val ok: Boolean,
    val records: List<FdroidAppRecord>,
    val githubLibrary: Map<String, GithubHint> = emptyMap(),
)

object ReleaseRefreshRepos {
    fun fetchAll(
        repos: List<FdroidRepo>,
        fetcher: FdroidIndexFetcher,
        store: FdroidIndexStore?,
        nowMs: Long,
        wanted: Set<String>,
        executor: ExecutorService?,
        clock: RefreshProgressClock,
        hostResolve: FdroidHostResolver? = null,
    ): List<FdroidRepoLoad> =
        ReleaseRefreshParallel.map(repos, RefreshHostGate.REPOS, executor) { repo ->
            loadOne(repo, fetcher, store, nowMs, wanted, clock, hostResolve)
        }

    private fun loadOne(
        repo: FdroidRepo,
        fetcher: FdroidIndexFetcher,
        store: FdroidIndexStore?,
        nowMs: Long,
        wanted: Set<String>,
        clock: RefreshProgressClock,
        hostResolve: FdroidHostResolver?,
    ): FdroidRepoLoad {
        val loc = RefreshLocations.label("F-Droid", repo.id)
        clock.begin(loc)
        val loaded = try {
            resolve(repo, fetcher, store, nowMs, wanted, hostResolve)
        } catch (error: Throwable) {
            RefreshTrace.line("fdroid ${repo.id} fail ${error.javaClass.simpleName}: ${error.message}")
            FdroidRepoLoad(repo.id, ok = false, records = emptyList())
        }
        clock.tick(loc)
        return loaded
    }

    private fun resolve(
        repo: FdroidRepo,
        fetcher: FdroidIndexFetcher,
        store: FdroidIndexStore?,
        nowMs: Long,
        wanted: Set<String>,
        hostResolve: FdroidHostResolver?,
    ): FdroidRepoLoad {
        val t0 = System.nanoTime()
        if (hostResolve != null && FdroidIndexBudget.hostResolve(repo)) {
            val found = hostResolve.resolve(repo, wanted)
            val library = FdroidGithubHints.hints(found, wanted)
            RefreshTrace.line(
                "fdroid ${repo.id} host-resolve ${found.size}apps library=${library.size} ${(System.nanoTime() - t0) / 1_000_000}ms",
            )
            return FdroidRepoLoad(repo.id, ok = true, records = found, githubLibrary = library)
        }
        val raw = FdroidRefreshFetch.load(
            repo,
            fetcher,
            store,
            nowMs,
            allowEmpty = hostResolve != null && FdroidIndexBudget.extraHostResolve(repo),
        )
        if (raw.isEmpty() && hostResolve != null && FdroidIndexBudget.extraHostResolve(repo)) {
            val found = hostResolve.resolve(repo, wanted)
            val library = FdroidGithubHints.hints(found, wanted)
            RefreshTrace.line("fdroid ${repo.id} extra-host ${found.size}apps")
            return FdroidRepoLoad(repo.id, ok = found.isNotEmpty(), records = found, githubLibrary = library)
        }
        val found = if (raw.isEmpty()) emptyList() else FdroidIndexParser.parse(raw, repo.id, wanted)
        val library = if (raw.isEmpty()) emptyMap() else FdroidGithubHints.harvest(raw, repo.id)
        RefreshTrace.line(
            "fdroid ${repo.id} ok ${raw.size}B ${found.size}apps library=${library.size} ${(System.nanoTime() - t0) / 1_000_000}ms",
        )
        return FdroidRepoLoad(repo.id, ok = true, records = found, githubLibrary = library)
    }
}
