package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.apkmirror.ApkMirrorBatchFetcher
import dev.foss.goldenpath.index.apkpure.ApkPureBatchFetcher
import dev.foss.goldenpath.index.aptoide.AptoideApkRef
import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.aptoide.AptoideScan
import dev.foss.goldenpath.index.aptoide.AptoideUpdatesFetcher
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.forge.GithubVerifiedStore
import dev.foss.goldenpath.index.forge.LeftoverHint
import dev.foss.goldenpath.index.forge.LeftoverSearchClient
import dev.foss.goldenpath.index.aurora.AuroraPlayDetails
import dev.foss.goldenpath.index.aurora.AuroraPlayScan
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
        leftoverClient: LeftoverSearchClient? = null,
        leftoverHints: Map<String, LeftoverHint> = emptyMap(),
        aptoideUpdatesFetcher: AptoideUpdatesFetcher = AptoideUpdatesFetcher {
            Result.failure(IllegalStateException("aptoide-batch"))
        },
        aurora: AuroraPlayDetails? = null,
    ) {
        val bags = ConcurrentHashMap<String, MutableList<RemoteReleaseOffer>>()
        seed.forEach { (pkg, offers) -> bags[pkg] = syncList(offers) }
        val dump = Thread {
            ReleaseRefreshDump.apply(
                apps, apkMirrorEnabled, apkPureEnabled, apkMirrorFetcher, apkPureFetcher, nowMs, clock,
            ) { pkg, offer -> commit(bags, pkg, offer) }
        }
        dump.start()
        if (aptoideEnabled) {
            AptoideScan.applyBatch(
                apps.map { AptoideApkRef(it.packageName, it.signingSha1, it.versionCode) },
                aptoideUpdatesFetcher,
                nowMs,
            ).forEach { (pkg, offer) -> commit(bags, pkg, offer) }
        }
        if (aurora != null) {
            AuroraPlayScan.applyBatch(apps.map { it.packageName }, aurora, nowMs)
                .forEach { (pkg, offer) -> commit(bags, pkg, offer) }
        }
        if (playOn) clock.planOutlet(RefreshOutletIds.PLAY, "Play", apps.size)
        if (aptoideEnabled) clock.planOutlet(RefreshOutletIds.APTOIDE, "Aptoide", apps.size)
        if (forgeOn) clock.planOutlet(RefreshOutletIds.GITHUB, "GitHub", apps.size)
        if (forgeOn && leftoverClient != null) {
            clock.planOutlet(RefreshOutletIds.LEFTOVER, "GitLab", apps.size)
        }
        val storeJobs = buildList {
            apps.forEach { app ->
                if (playOn) add(OutletJob(app, OutletKind.Play))
                if (aptoideEnabled) add(OutletJob(app, OutletKind.Aptoide))
            }
        }
        val forgeJobs = if (forgeOn) apps.map { OutletJob(it, OutletKind.GitHub) } else emptyList()
        listOf(storeJobs, forgeJobs).forEach { jobs ->
            ReleaseRefreshParallel.map(jobs, RefreshHostGate.APPS, executor) { job ->
                ReleaseRefreshOutlet.run(
                    job, bags, playClient, aptoideFetcher, gitHubClient, nowMs, gate, clock,
                    knownRepos, verifiedStore, searchUnknowns, leftoverClient, leftoverHints, aurora,
                ) { pkg, offer -> commit(bags, pkg, offer) }
            }
        }
        dump.join()
        if (playOn) RefreshOutletBoard.noteFinished(RefreshOutletIds.PLAY)
        if (aptoideEnabled) RefreshOutletBoard.noteFinished(RefreshOutletIds.APTOIDE)
        if (forgeOn) RefreshOutletBoard.noteFinished(RefreshOutletIds.GITHUB)
        if (forgeOn && leftoverClient != null) RefreshOutletBoard.noteFinished(RefreshOutletIds.LEFTOVER)
        ReleaseRefreshComplete.write(
            apps,
            { pkg -> bag(bags, pkg) },
            ReleaseRefreshComplete.searched(
                repos, playOn, aptoideEnabled, forgeOn, apkMirrorEnabled, apkPureEnabled,
            ),
        )
    }

    private fun syncList(offers: List<RemoteReleaseOffer>): MutableList<RemoteReleaseOffer> =
        Collections.synchronizedList(offers.toMutableList())

    private fun bag(
        bags: ConcurrentHashMap<String, MutableList<RemoteReleaseOffer>>,
        pkg: String,
    ): List<RemoteReleaseOffer> {
        val list = bags.getOrPut(pkg) { syncList(emptyList()) }
        synchronized(list) { return list.toList() }
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
