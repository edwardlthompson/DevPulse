package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.apkmirror.ApkMirrorBatchFetcher
import dev.foss.goldenpath.index.apkpure.ApkPureBatchFetcher
import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import dev.foss.goldenpath.index.fdroid.FdroidCategoryStore
import dev.foss.goldenpath.index.fdroid.FdroidHostResolver
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexStore
import dev.foss.goldenpath.index.fdroid.FdroidNotes
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.forge.FdroidGithubHints
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.LeftoverSearchClient
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.forge.GithubVerifiedStore
import dev.foss.goldenpath.index.forge.PastedRepoCodec
import dev.foss.goldenpath.index.forge.PastedRepoStore
import dev.foss.goldenpath.index.play.PlayPageClient
import java.util.concurrent.ExecutorService

object ReleaseRefresh {
    fun fdroidOffers(
        records: List<FdroidAppRecord>,
        wanted: Set<String>,
    ): Map<String, List<RemoteReleaseOffer>> {
        val hits = records.filter { it.packageName in wanted }
        FdroidNotes.remember(hits, wanted)
        return hits.groupBy { it.packageName }.mapValues { (_, group) ->
            group.map { rec ->
                val source = ListingChannels.sourceForRepo(rec.repoId)
                val page = UpdateUrls.forFdroid(rec.packageName, rec.repoId, rec.sourceCode)
                RemoteReleaseOffer(source, rec.lastUpdatedMs, rec.suggestedVersionName, page)
            }
        }
    }

    fun fdroidPicks(records: List<FdroidAppRecord>, wanted: Set<String>): Map<String, RemoteReleasePick> =
        fdroidOffers(records, wanted).mapValues { RemoteReleaseRollup.from(it.value) }

    fun githubHints(
        records: List<FdroidAppRecord>,
        wanted: Set<String>,
        verified: Map<String, String> = emptyMap(),
        pasted: Map<String, String> = emptyMap(),
    ): Map<String, GithubHint> {
        val fromStore = verified.mapValues { GithubHint(it.value) }
        return fromStore + FdroidGithubHints.hints(records, wanted) + PastedRepoCodec.hints(pasted)
    }

    @Suppress("UNUSED_PARAMETER")
    fun run(
        apps: List<InstalledApp>,
        repos: List<FdroidRepo>,
        aptoideEnabled: Boolean,
        fdroidFetcher: FdroidIndexFetcher,
        aptoideFetcher: AptoideMetaFetcher,
        nowMs: Long,
        playClient: PlayPageClient? = null,
        gitHubClient: GitHubSearchClient? = null,
        indexStore: FdroidIndexStore? = null,
        sleepMs: (Long) -> Unit = { ms -> if (ms > 0) Thread.sleep(ms) },
        onProgress: (RefreshProgress) -> Unit = {},
        executor: ExecutorService? = null,
        hostGate: RefreshHostGate? = null,
        verifiedStore: GithubVerifiedStore? = null,
        searchUnknowns: Boolean = false,
        apkMirrorEnabled: Boolean = false,
        apkPureEnabled: Boolean = false,
        apkMirrorFetcher: ApkMirrorBatchFetcher = ApkMirrorBatchFetcher { Result.success("") },
        apkPureFetcher: ApkPureBatchFetcher = ApkPureBatchFetcher { Result.success("") },
        hostResolve: FdroidHostResolver? = null,
        leftoverClient: LeftoverSearchClient? = null,
        categoryStore: FdroidCategoryStore? = null,
        pastedStore: PastedRepoStore? = null,
    ): Map<String, RemoteReleasePick> {
        val userApps = apps.filter { !it.isSystemApp }
        val wantedSet = userApps.map { it.packageName }.toSet()
        val playOn = playClient != null
        val forgeOn = gitHubClient != null
        val noRemote = !playOn && !aptoideEnabled && !forgeOn && !apkMirrorEnabled && !apkPureEnabled
        if (repos.isEmpty() && (userApps.isEmpty() || noRemote)) {
            onProgress(RefreshProgress(1, 1, "idle"))
            return emptyMap()
        }
        val clock = RefreshProgressClock(onProgress)
        clock.addWork(
            RefreshLocations.total(
                repos.size, userApps.size, playOn, aptoideEnabled, forgeOn, apkMirrorEnabled, apkPureEnabled,
            ),
        )
        clock.begin("refresh start repos=${repos.size} apps=${userApps.size}")
        val loaded = ReleaseRefreshRepos.fetchAll(
            repos, fdroidFetcher, indexStore, nowMs, wantedSet, executor, clock, hostResolve,
        )
        val records = loaded.flatMap { it.records }
        categoryStore?.putAll(records)
        val okRepos = loaded.filter { it.ok }.map { it.repoId }.toSet()
        val byPackage = FdroidIndexMisses.merge(
            fdroidOffers(records, wantedSet),
            FdroidIndexMisses.offers(wantedSet, okRepos, records),
        )
        RemoteReleaseMemory.putAll(byPackage.mapValues { RemoteReleaseRollup.from(it.value) })
        val library = FdroidGithubHints.mergeLibrary(
            verifiedStore?.load().orEmpty(),
            loaded.map { it.repoId to it.githubLibrary },
        )
        val knownRepos = library + FdroidGithubHints.hints(records, wantedSet) +
            PastedRepoCodec.hints(pastedStore?.load().orEmpty())
        verifiedStore?.save(knownRepos.mapValues { it.value.ownerRepo })
        RefreshTrace.line("github library ${library.size} persisted")
        ReleaseRefreshWaves.storeThenForge(
            userApps, byPackage, playOn, aptoideEnabled, forgeOn,
            playClient, aptoideFetcher, gitHubClient, nowMs, repos,
            executor, hostGate ?: RefreshHostGate(), clock,
            knownRepos, verifiedStore, searchUnknowns,
            apkMirrorEnabled, apkPureEnabled, apkMirrorFetcher, apkPureFetcher,
            leftoverClient,
        )
        RefreshTrace.line("refresh done locations=${clock.done}/${clock.total}")
        return RemoteReleaseMemory.byPackage.filterKeys { it in wantedSet }
    }
}
