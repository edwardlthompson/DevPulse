package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.apkmirror.ApkMirrorBatchFetcher
import dev.foss.goldenpath.index.apkpure.ApkPureBatchFetcher
import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.forge.GithubVerifiedStore
import dev.foss.goldenpath.index.play.PlayPageClient
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService

object ReleaseRefreshWaves {
    fun storeThenForge(
        apps: List<InstalledApp>,
        seed: Map<String, List<RemoteReleaseOffer>>,
        playOn: Boolean,
        aptoideEnabled: Boolean,
        forgeOn: Boolean,
        playClient: PlayPageClient?,
        aptoideFetcher: AptoideMetaFetcher,
        gitHubClient: GitHubSearchClient?,
        nowMs: Long,
        repos: List<FdroidRepo>,
        executor: ExecutorService?,
        gate: RefreshHostGate,
        clock: RefreshProgressClock,
        knownRepos: Map<String, GithubHint> = emptyMap(),
        verifiedStore: GithubVerifiedStore? = null,
        searchUnknowns: Boolean = false,
        apkMirrorEnabled: Boolean = false,
        apkPureEnabled: Boolean = false,
        apkMirrorFetcher: ApkMirrorBatchFetcher = ApkMirrorBatchFetcher { Result.success("") },
        apkPureFetcher: ApkPureBatchFetcher = ApkPureBatchFetcher { Result.success("") },
    ) {
        val bags = ConcurrentHashMap<String, MutableList<RemoteReleaseOffer>>()
        seed.forEach { (pkg, offers) -> bags[pkg] = syncList(offers) }
        ReleaseRefreshDump.apply(
            apps = apps,
            apkMirrorEnabled = apkMirrorEnabled,
            apkPureEnabled = apkPureEnabled,
            apkMirrorFetcher = apkMirrorFetcher,
            apkPureFetcher = apkPureFetcher,
            nowMs = nowMs,
            clock = clock,
        ) { pkg, offer -> commit(bags, pkg, offer) }
        val jobs = buildList {
            apps.forEach { app ->
                if (playOn) add(OutletJob(app, Kind.Play))
                if (aptoideEnabled) add(OutletJob(app, Kind.Aptoide))
                if (forgeOn) add(OutletJob(app, Kind.GitHub))
            }
        }
        ReleaseRefreshParallel.map(jobs, RefreshHostGate.APPS, executor) { job ->
            runJob(
                job, bags, playClient, aptoideFetcher, gitHubClient, nowMs, gate, clock,
                knownRepos, verifiedStore, searchUnknowns,
            )
        }
        ReleaseRefreshComplete.write(
            apps,
            { pkg -> bag(bags, pkg) },
            ReleaseRefreshComplete.searched(
                repos, playOn, aptoideEnabled, forgeOn, apkMirrorEnabled, apkPureEnabled,
            ),
        )
    }

    private enum class Kind { Play, Aptoide, GitHub }

    private data class OutletJob(val app: InstalledApp, val kind: Kind)

    private fun syncList(offers: List<RemoteReleaseOffer>): MutableList<RemoteReleaseOffer> =
        Collections.synchronizedList(offers.toMutableList())

    private fun bag(
        bags: ConcurrentHashMap<String, MutableList<RemoteReleaseOffer>>,
        pkg: String,
    ): List<RemoteReleaseOffer> {
        val list = bags.getOrPut(pkg) { syncList(emptyList()) }
        synchronized(list) { return list.toList() }
    }

    private fun runJob(
        job: OutletJob,
        bags: ConcurrentHashMap<String, MutableList<RemoteReleaseOffer>>,
        playClient: PlayPageClient?,
        aptoideFetcher: AptoideMetaFetcher,
        gitHubClient: GitHubSearchClient?,
        nowMs: Long,
        gate: RefreshHostGate,
        clock: RefreshProgressClock,
        knownRepos: Map<String, GithubHint>,
        verifiedStore: GithubVerifiedStore?,
        searchUnknowns: Boolean,
    ) {
        val app = job.app
        val pkg = app.packageName
        val loc = RefreshLocations.label(job.kind.name, app.label, pkg)
        clock.begin(loc)
        val offer = try {
            when (job.kind) {
                Kind.Play -> gate.play { ReleaseRefreshProbes.play(pkg, playClient!!) }
                Kind.Aptoide -> gate.aptoide { ReleaseRefreshProbes.aptoide(pkg, aptoideFetcher, nowMs) }
                Kind.GitHub -> gate.github {
                    ReleaseRefreshProbes.github(
                        pkg,
                        app.label,
                        gitHubClient!!,
                        knownRepos[pkg],
                        searchUnknowns,
                    ) { ownerRepo ->
                        verifiedStore?.put(pkg, ownerRepo)
                    }
                }
            }
        } catch (error: Throwable) {
            RefreshTrace.line("${job.kind.name.lowercase()} $pkg fail ${error.javaClass.simpleName}: ${error.message}")
            null
        }
        commit(bags, pkg, offer)
        clock.tick(loc)
    }

    private fun commit(
        bags: ConcurrentHashMap<String, MutableList<RemoteReleaseOffer>>,
        pkg: String,
        extra: RemoteReleaseOffer?,
    ) {
        val merged = bags.getOrPut(pkg) { syncList(emptyList()) }
        synchronized(merged) {
            mergeOffer(merged, extra)
            RemoteReleaseMemory.putAll(mapOf(pkg to RemoteReleaseRollup.from(merged.toList())))
        }
    }

    internal fun mergeOffer(into: MutableList<RemoteReleaseOffer>, extra: RemoteReleaseOffer?) {
        if (extra == null) return
        into.removeAll { it.source == extra.source }
        into += extra
    }
}
