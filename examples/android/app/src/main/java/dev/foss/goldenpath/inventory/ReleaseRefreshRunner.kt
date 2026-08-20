package dev.foss.goldenpath.inventory

import android.content.Context
import android.util.Log
import dev.foss.goldenpath.index.aptoide.AptoideHttpFetcher
import dev.foss.goldenpath.index.fdroid.FdroidEnabledRepos
import dev.foss.goldenpath.index.fdroid.FdroidIndexHttpFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexStore
import dev.foss.goldenpath.index.fdroid.FdroidRepoPreferences
import dev.foss.goldenpath.index.forge.DataStoreForgeTokenStore
import dev.foss.goldenpath.index.forge.FileGithubVerifiedStore
import dev.foss.goldenpath.index.forge.GitHubSearchHttp
import dev.foss.goldenpath.index.play.PlayHttpFetcher
import java.io.File
import kotlinx.coroutines.flow.first

object ReleaseRefreshRunner {
    suspend fun run(context: Context, onProgress: (RefreshProgress) -> Unit): Int {
        RefreshTrace.emit = { Log.i("DevPulse", it) }
        val prefs = InventoryPreferences(context)
        val playOn = prefs.playLookupEnabled.first()
        val forgeOn = prefs.forgeLookupEnabled.first()
        val searchUnknowns = prefs.forgeLookupSearchUnknowns.first()
        val result = ReleaseRefresh.run(
            apps = PackageManagerPackageCatalog(context.packageManager).listInstalled(),
            repos = FdroidEnabledRepos.list(FdroidRepoPreferences(context)),
            aptoideEnabled = prefs.aptoideLookupEnabled.first(),
            fdroidFetcher = FdroidIndexHttpFetcher,
            aptoideFetcher = AptoideHttpFetcher,
            nowMs = System.currentTimeMillis(),
            playClient = PlayHttpFetcher.takeIf { playOn },
            gitHubClient = if (forgeOn) GitHubSearchHttp(DataStoreForgeTokenStore(context).getToken()) else null,
            indexStore = FdroidIndexStore(File(context.filesDir, "fdroid-index")),
            verifiedStore = FileGithubVerifiedStore(File(context.filesDir, "github_verified.tsv")),
            searchUnknowns = searchUnknowns,
            onProgress = onProgress,
        )
        prefs.setLastScanAtMs(System.currentTimeMillis())
        return result.size
    }
}
