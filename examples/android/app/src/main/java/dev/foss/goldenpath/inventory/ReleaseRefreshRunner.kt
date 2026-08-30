package dev.foss.goldenpath.inventory

import android.content.Context
import android.util.Log
import dev.foss.goldenpath.index.apkmirror.ApkMirrorHttpFetcher
import dev.foss.goldenpath.index.apkpure.ApkPureHttpFetcher
import dev.foss.goldenpath.index.aptoide.AptoideHttpFetcher
import dev.foss.goldenpath.index.aptoide.AptoideUpdatesHttp
import dev.foss.goldenpath.index.aurora.AuroraPlayLive
import dev.foss.goldenpath.index.aurora.AuroraPlayWarm
import dev.foss.goldenpath.index.fdroid.FdroidEnabledRepos
import dev.foss.goldenpath.index.fdroid.FdroidIndexHttpFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexStore
import dev.foss.goldenpath.index.fdroid.FdroidRepoPreferences
import dev.foss.goldenpath.index.fdroid.FdroidHostHttp
import dev.foss.goldenpath.index.fdroid.FdroidNameCatalog
import dev.foss.goldenpath.index.fdroid.FileFdroidCategoryStore
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.index.forge.FileGithubVerifiedStore
import dev.foss.goldenpath.index.forge.FilePastedRepoStore
import dev.foss.goldenpath.index.forge.ForgeHost
import dev.foss.goldenpath.index.forge.GitHubSearchHttp
import dev.foss.goldenpath.index.forge.LeftoverForgeHttp
import dev.foss.goldenpath.index.play.PlayHttpFetcher
import dev.foss.goldenpath.index.play.WaybackHttpFetcher
import dev.foss.goldenpath.index.play.WaybackPlay
import dev.foss.goldenpath.network.NetworkUnmetered
import java.io.File
import kotlinx.coroutines.flow.first

object ReleaseRefreshRunner {
    suspend fun run(
        context: Context,
        onProgress: (RefreshProgress) -> Unit,
        wanted: Collection<String> = emptySet(),
    ): Int {
        RefreshTrace.emit = { Log.i("DevPulse", it) }
        val prefs = InventoryPreferences(context)
        val playOn = prefs.playLookupEnabled.first()
        val forgeOn = prefs.forgeLookupEnabled.first()
        val searchUnknowns = prefs.forgeLookupSearchUnknowns.first()
        if (playOn) AuroraPlayWarm.session(context)
        WaybackPlay.client = WaybackHttpFetcher.takeIf { playOn }
        val paceFile = File(context.filesDir, "refresh_pace.tsv")
        val successFile = File(context.filesDir, "refresh_success.tsv")
        val failFile = File(context.filesDir, "refresh_fail.tsv")
        RefreshPaceBook.hydrate(RefreshPaceFile.load(paceFile))
        RefreshSuccessBook.hydrate(RefreshPaceFile.load(successFile))
        RefreshFailBook.hydrate(RefreshPaceFile.load(failFile))
        RefreshResume.persistDir = context.filesDir
        DumpChunkBook.persistDir = context.filesDir
        DumpChunkBook.hydrate(context.filesDir)
        val startedAt = System.currentTimeMillis()
        return try {
        val result = ReleaseRefresh.run(
            apps = RefreshScope.apps(
                PackageManagerPackageCatalog(context.packageManager).listInstalled(),
                wanted,
            ),
            repos = FdroidEnabledRepos.list(FdroidRepoPreferences(context)),
            aptoideEnabled = prefs.aptoideLookupEnabled.first(),
            fdroidFetcher = FdroidIndexHttpFetcher,
            aptoideFetcher = AptoideHttpFetcher,
            aptoideUpdatesFetcher = AptoideUpdatesHttp,
            nowMs = System.currentTimeMillis(),
            playClient = PlayHttpFetcher.takeIf { playOn },
            aurora = AuroraPlayLive.details(context).takeIf { playOn },
            gitHubClient = if (forgeOn) GitHubSearchHttp(EncryptedForgeTokenStore.wrap(context).getToken()) else null,
            indexStore = FdroidIndexStore(File(context.filesDir, "fdroid-index")),
            verifiedStore = FileGithubVerifiedStore(File(context.filesDir, "github_verified.tsv")),
            searchUnknowns = searchUnknowns,
            hostResolve = FdroidHostHttp(),
            leftoverClient = leftoverHttp(context),
            categoryStore = FileFdroidCategoryStore(File(context.filesDir, "fdroid_categories.tsv")),
            pastedStore = FilePastedRepoStore(File(context.filesDir, "pasted_repos.tsv")),
            nameCatalog = runCatching { FdroidNameCatalog.fromAssets(context.assets) }
                .getOrNull()
                ?.also {
                    RefreshTrace.line("fdroid catalog official=${it.size("official")} izzy=${it.size("izzy")}")
                },
            apkMirrorEnabled = prefs.apkMirrorLookupEnabled.first(),
            apkPureEnabled = prefs.apkPureLookupEnabled.first(),
            apkMirrorFetcher = ApkMirrorHttpFetcher,
            apkPureFetcher = ApkPureHttpFetcher,
            onProgress = onProgress,
        )
        val snaps = ReleaseRefreshRuntime.progress.value.outlets
        RefreshSuccessBook.capture(snaps)
        RefreshFailBook.capture(snaps)
        RefreshPaceFile.save(paceFile, RefreshPaceBook.snapshot())
        RefreshPaceFile.save(successFile, RefreshSuccessBook.snapshot())
        RefreshPaceFile.save(failFile, RefreshFailBook.snapshot())
        if (snaps.isNotEmpty() && snaps.all { it.finishedAtMs != null }) RefreshResume.clear()
        prefs.setLastScanAtMs(System.currentTimeMillis())
        val progress = ReleaseRefreshRuntime.progress.value
        val outlets = progress.outlets.joinToString(";") { "${it.id}=${it.elapsedMs}" }
        PulseHistory.note(
            context.filesDir,
            "refresh",
            System.currentTimeMillis() - startedAt,
            result.size,
            "locations=${progress.total};$outlets",
        )
        ReleaseRefreshRuntime.finish()
        if (prefs.updatePrefetchEnabled.first()) {
            UpdatePrefetch.run(
                enabled = true,
                unmetered = NetworkUnmetered.isUnmetered(context),
                cacheDir = File(context.cacheDir, "updates"),
                artifacts = UpdateArtifactMemory.byPackage.values.flatten(),
                fetch = ApkHttpFetcher,
                inspect = { file -> ApkArchiveIdentity.inspect(context.packageManager, file) },
                installed = { pkg -> ApkArchiveIdentity.installed(context.packageManager, pkg) },
            )
        }
        result.size
        } finally {
            WaybackPlay.client = null
            AuroraPlayLive.releaseSession()
        }
    }

    private fun leftoverHttp(context: Context): LeftoverForgeHttp {
        val store = EncryptedForgeTokenStore.create(context)
        return LeftoverForgeHttp(
            store?.leftover(ForgeHost.GitLab),
            store?.leftover(ForgeHost.Codeberg),
        )
    }
}
