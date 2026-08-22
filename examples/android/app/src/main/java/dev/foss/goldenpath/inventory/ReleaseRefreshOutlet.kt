package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.forge.GithubVerifiedStore
import dev.foss.goldenpath.index.forge.LeftoverHint
import dev.foss.goldenpath.index.forge.LeftoverSearchClient
import dev.foss.goldenpath.index.aurora.AuroraPlayDetails
import dev.foss.goldenpath.index.play.PlayPageClient
import java.util.concurrent.ConcurrentHashMap

internal enum class OutletKind { Play, Aptoide, GitHub }

internal data class OutletJob(val app: InstalledApp, val kind: OutletKind)

internal object ReleaseRefreshOutlet {
    fun run(
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
        leftoverClient: LeftoverSearchClient?,
        leftoverHints: Map<String, LeftoverHint>,
        aurora: AuroraPlayDetails?,
        commit: (String, RemoteReleaseOffer?) -> Unit,
    ) {
        val app = job.app
        val pkg = app.packageName
        val oid = id(job.kind)
        val loc = RefreshLocations.label(job.kind.name, app.label, pkg)
        clock.begin(loc)
        clock.outletAt(oid, app.label)
        if (job.kind == OutletKind.GitHub && leftoverClient != null) {
            clock.outletAt(RefreshOutletIds.LEFTOVER, app.label)
        }
        commit(pkg, if (RefreshSkip.stopped(oid)) null else probe(job, playClient, aptoideFetcher, gitHubClient, nowMs, gate, knownRepos, verifiedStore, searchUnknowns, leftoverClient, leftoverHints[pkg], aurora))
        clock.outletTick(oid)
        if (job.kind == OutletKind.GitHub && leftoverClient != null) {
            clock.outletTick(RefreshOutletIds.LEFTOVER)
        }
        clock.tick(loc)
    }

    private fun id(kind: OutletKind): String = when (kind) {
        OutletKind.Play -> RefreshOutletIds.PLAY
        OutletKind.Aptoide -> RefreshOutletIds.APTOIDE
        OutletKind.GitHub -> RefreshOutletIds.GITHUB
    }

    private fun probe(
        job: OutletJob,
        playClient: PlayPageClient?,
        aptoideFetcher: AptoideMetaFetcher,
        gitHubClient: GitHubSearchClient?,
        nowMs: Long,
        gate: RefreshHostGate,
        knownRepos: Map<String, GithubHint>,
        verifiedStore: GithubVerifiedStore?,
        searchUnknowns: Boolean,
        leftoverClient: LeftoverSearchClient?,
        leftoverHint: LeftoverHint?,
        aurora: AuroraPlayDetails?,
    ): RemoteReleaseOffer? {
        val pkg = job.app.packageName
        return try {
            when (job.kind) {
                OutletKind.Play -> gate.play { ReleaseRefreshProbes.play(pkg, playClient, nowMs, aurora) }
                OutletKind.Aptoide -> gate.aptoide { ReleaseRefreshProbes.aptoide(pkg, aptoideFetcher, nowMs) }
                OutletKind.GitHub -> gate.github {
                    ReleaseRefreshProbes.github(
                        packageName = pkg,
                        label = job.app.label,
                        client = gitHubClient!!,
                        hint = knownRepos[pkg],
                        searchUnknowns = searchUnknowns,
                        leftover = leftoverClient,
                        leftoverHint = leftoverHint,
                        onVerified = { ownerRepo -> verifiedStore?.put(pkg, ownerRepo) },
                        nowMs = nowMs,
                    )
                }
            }
        } catch (error: Throwable) {
            RefreshTrace.line("${job.kind.name.lowercase()} $pkg fail ${error.javaClass.simpleName}: ${error.message}")
            null
        }
    }
}
