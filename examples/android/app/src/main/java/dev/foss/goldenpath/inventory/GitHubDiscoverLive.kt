package dev.foss.goldenpath.inventory

import android.content.Context
import android.os.Build
import android.util.Log
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.index.forge.FileGithubSearchSkipStore
import dev.foss.goldenpath.index.forge.FileGithubVerifiedStore
import dev.foss.goldenpath.index.forge.GitHubSearchHttp
import dev.foss.goldenpath.index.forge.GithubHintFiles
import dev.foss.goldenpath.network.NetworkUnmetered
import java.io.File
import kotlinx.coroutines.flow.first

object GitHubDiscoverLive {
    suspend fun run(context: Context): GitHubDiscoverTick {
        val apps = PackageManagerPackageCatalog(context.packageManager).listInstalled()
        val shipped = GithubCatalogLive.loadShipped(context)
        val library = GithubHintFiles.load(context.filesDir, shipped)
        val skipStore = FileGithubSearchSkipStore(File(context.filesDir, "github_search_skip.tsv"))
        val verified = FileGithubVerifiedStore(File(context.filesDir, "github_verified.tsv"))
        val client = GitHubSearchHttp(EncryptedForgeTokenStore.wrap(context).getToken())
        val tick = GitHubDiscover.tick(
            apps = apps,
            library = library,
            skip = skipStore.load(),
            client = client,
            verified = verified,
            nowMs = System.currentTimeMillis(),
            listed = { ReleaseRefreshProbes.storeListed(it) },
        )
        skipStore.save(tick.skip)
        Log.i("DevPulse", "github discover searched=${tick.searched} hits=${tick.hits} misses=${tick.misses} left=${tick.remaining}")
        return tick
    }

    suspend fun allowed(context: Context): Boolean {
        val prefs = InventoryPreferences(context)
        if (!prefs.forgeLookupEnabled.first()) return false
        val canScan = QueryAllPackagesGate.canScan(
            prefs.queryAllPackagesAcknowledged.first(),
            Build.VERSION.SDK_INT,
        )
        if (!canScan) return false
        val wifiOnly = RefreshWifiPrefs(context).enabled.first()
        return RefreshWifiOnly.allow(wifiOnly, NetworkUnmetered.isUnmetered(context))
    }
}
