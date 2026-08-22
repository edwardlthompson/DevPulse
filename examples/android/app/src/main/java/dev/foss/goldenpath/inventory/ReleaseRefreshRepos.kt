package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import dev.foss.goldenpath.index.fdroid.FdroidHostResolver
import dev.foss.goldenpath.index.fdroid.FdroidIndexBudget
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexParser
import dev.foss.goldenpath.index.fdroid.FdroidIndexStore
import dev.foss.goldenpath.index.fdroid.FdroidNameCatalog
import dev.foss.goldenpath.index.fdroid.FdroidProbeWanted
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
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
        nameCatalog: FdroidNameCatalog? = null,
    ): List<FdroidRepoLoad> {
        val officialOn = repos.any { it.id == "official" || it.kind == FdroidRepoKind.Official }
        return ReleaseRefreshParallel.map(repos, RefreshHostGate.REPOS, executor) { repo ->
            val id = RefreshOutletIds.fdroid(repo.id)
            clock.planOutlet(id, repo.id, wanted.size)
            when {
                RefreshSkip.stopped(id) -> skip(repo, clock, "stopped")
                hostResolve != null && FdroidIndexBudget.skipArchiveHost(officialOn, repo) ->
                    skip(repo, clock, "official covers")
                else -> loadOne(repo, fetcher, store, nowMs, wanted, clock, hostResolve, nameCatalog)
            }
        }
    }

    private fun loadOne(
        repo: FdroidRepo,
        fetcher: FdroidIndexFetcher,
        store: FdroidIndexStore?,
        nowMs: Long,
        wanted: Set<String>,
        clock: RefreshProgressClock,
        hostResolve: FdroidHostResolver?,
        nameCatalog: FdroidNameCatalog?,
    ): FdroidRepoLoad {
        val loc = RefreshLocations.label("F-Droid", repo.id)
        val id = RefreshOutletIds.fdroid(repo.id)
        clock.begin(loc)
        clock.outletAt(id, repo.id)
        val loaded = try {
            resolve(repo, fetcher, store, nowMs, wanted, hostResolve, nameCatalog)
        } catch (error: Throwable) {
            RefreshTrace.line("fdroid ${repo.id} fail ${error.javaClass.simpleName}: ${error.message}")
            FdroidRepoLoad(repo.id, ok = false, records = emptyList())
        }
        RefreshOutletBoard.fill(id)
        clock.pulse()
        clock.tick(loc)
        RefreshOutletBoard.noteFinished(id)
        return loaded
    }

    private fun skip(repo: FdroidRepo, clock: RefreshProgressClock, why: String): FdroidRepoLoad {
        val loc = RefreshLocations.label("F-Droid", repo.id)
        val id = RefreshOutletIds.fdroid(repo.id)
        clock.begin(loc)
        RefreshTrace.line("fdroid ${repo.id} skip host-resolve ($why)")
        RefreshOutletBoard.fill(id)
        clock.pulse()
        clock.tick(loc)
        RefreshOutletBoard.noteFinished(id)
        return FdroidRepoLoad(repo.id, ok = true, records = emptyList())
    }

    private fun resolve(
        repo: FdroidRepo,
        fetcher: FdroidIndexFetcher,
        store: FdroidIndexStore?,
        nowMs: Long,
        wanted: Set<String>,
        hostResolve: FdroidHostResolver?,
        nameCatalog: FdroidNameCatalog?,
    ): FdroidRepoLoad {
        val t0 = System.nanoTime()
        val slice = FdroidProbeWanted.slice(repo, wanted, nowMs, nameCatalog)
        val work = (slice.stale.size + slice.cached.size).coerceAtLeast(1)
        RefreshOutletBoard.resize(RefreshOutletIds.fdroid(repo.id), work, slice.cached.size)
        RefreshProgressClock.pulseActive()
        if (hostResolve != null && FdroidIndexBudget.hostResolve(repo)) {
            val found = if (slice.stale.isEmpty()) emptyList() else hostResolve.resolve(repo, slice.stale)
            val records = slice.cached + found
            val library = FdroidGithubHints.hints(records, wanted)
            RefreshTrace.line(
                "fdroid ${repo.id} host-resolve ${found.size}apps library=${library.size} ${(System.nanoTime() - t0) / 1_000_000}ms",
            )
            return FdroidRepoLoad(repo.id, ok = true, records = records, githubLibrary = library)
        }
        val raw = FdroidRefreshFetch.load(
            repo,
            fetcher,
            store,
            nowMs,
            allowEmpty = hostResolve != null && FdroidIndexBudget.extraHostResolve(repo),
        )
        if (raw.isEmpty() && hostResolve != null && FdroidIndexBudget.extraHostResolve(repo)) {
            if (!FdroidIndexBudget.extraHostAllowed(slice.stale.size)) {
                RefreshTrace.line("fdroid ${repo.id} skip extra-host ${slice.stale.size}apps")
                return FdroidRepoLoad(repo.id, ok = true, records = slice.cached)
            }
            val found = if (slice.stale.isEmpty()) emptyList() else hostResolve.resolve(repo, slice.stale)
            val records = slice.cached + found
            val library = FdroidGithubHints.hints(records, wanted)
            RefreshTrace.line("fdroid ${repo.id} extra-host ${found.size}apps")
            return FdroidRepoLoad(repo.id, ok = found.isNotEmpty() || slice.cached.isNotEmpty(), records = records, githubLibrary = library)
        }
        val found = if (raw.isEmpty()) emptyList() else FdroidIndexParser.parse(raw, repo.id, slice.stale)
        val records = slice.cached + found
        val library = if (raw.isEmpty()) emptyMap() else FdroidGithubHints.harvest(raw, repo.id)
        RefreshTrace.line(
            "fdroid ${repo.id} ok ${raw.size}B ${records.size}apps library=${library.size} ${(System.nanoTime() - t0) / 1_000_000}ms",
        )
        return FdroidRepoLoad(repo.id, ok = true, records = records, githubLibrary = library)
    }
}
