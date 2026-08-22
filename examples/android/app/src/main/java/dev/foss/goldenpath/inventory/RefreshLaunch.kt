package dev.foss.goldenpath.inventory

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dev.foss.goldenpath.index.fdroid.FdroidRepoCatalog
import dev.foss.goldenpath.index.fdroid.FdroidRepoPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object RefreshLaunch {
    const val EXTRA = "refresh"
    const val EXTRA_APK_MIRROR = "apk_mirror"
    const val EXTRA_APK_PURE = "apk_pure"
    const val EXTRA_ALL_SOURCES = "all_sources"
    const val EXTRA_LISTING = "listing_package"
    const val EXTRA_LISTING_SOURCE = "listing_source"

    fun requested(intent: Intent?): Boolean =
        intent?.getBooleanExtra(EXTRA, false) == true

    fun listingPackage(intent: Intent?): String? =
        intent?.getStringExtra(EXTRA_LISTING)?.trim()?.ifEmpty { null }

    fun listingSource(intent: Intent?): RemoteReleasedSource =
        runCatching {
            RemoteReleasedSource.valueOf(intent?.getStringExtra(EXTRA_LISTING_SOURCE)?.trim().orEmpty())
        }.getOrDefault(RemoteReleasedSource.Play)

    fun flag(intent: Intent?, key: String): Boolean? =
        intent?.takeIf { it.hasExtra(key) }?.getBooleanExtra(key, false)

    fun maybeStart(activity: ComponentActivity, intent: Intent?) {
        val refresh = requested(intent)
        val allSources = flag(intent, EXTRA_ALL_SOURCES) == true
        val mirror = flag(intent, EXTRA_APK_MIRROR)
        val pure = flag(intent, EXTRA_APK_PURE)
        val download = DownloadLaunch.packageName(intent)
        val downloadUrl = DownloadLaunch.urlOverride(intent)
        val listing = listingPackage(intent)
        val source = listingSource(intent)
        val updateAll = UpdateAllLaunch.requested(intent)
        intent?.removeExtra(EXTRA)
        intent?.removeExtra(EXTRA_ALL_SOURCES)
        intent?.removeExtra(EXTRA_APK_MIRROR)
        intent?.removeExtra(EXTRA_APK_PURE)
        intent?.removeExtra(DownloadLaunch.EXTRA)
        intent?.removeExtra(DownloadLaunch.EXTRA_URL)
        intent?.removeExtra(EXTRA_LISTING)
        intent?.removeExtra(EXTRA_LISTING_SOURCE)
        intent?.removeExtra(UpdateAllLaunch.EXTRA)
        if (refresh) {
            activity.lifecycleScope.launch {
                val prefs = InventoryPreferences(activity)
                if (allSources) enableAllSources(activity, prefs)
                if (mirror != null) prefs.setApkMirrorLookupEnabled(mirror)
                if (pure != null) prefs.setApkPureLookupEnabled(pure)
                val ack = prefs.queryAllPackagesAcknowledged.first()
                if (!QueryAllPackagesGate.canScan(ack, Build.VERSION.SDK_INT)) {
                    Log.i("DevPulse", "refresh launch skipped: query-all-packages not acknowledged")
                    return@launch
                }
                Log.i("DevPulse", "refresh launch start")
                ReleaseRefreshService.start(activity)
            }
        }
        if (download != null) {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                DownloadLaunch.run(activity, download, url = downloadUrl)
            }
        }
        if (listing != null) {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                val method = InventoryPreferences(activity).installMethod.first()
                val result = ListingInstallLive.run(activity, listing, source, null, method)
                Log.i("DevPulse", "listing smoke $listing ${source.name} ${result::class.simpleName}")
            }
        }
        if (updateAll) {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                UpdateAllLaunch.run(activity)
            }
        }
    }

    private suspend fun enableAllSources(activity: ComponentActivity, prefs: InventoryPreferences) {
        prefs.setPlayLookupEnabled(true)
        prefs.setForgeLookupEnabled(true)
        prefs.setForgeLookupSearchUnknowns(false)
        prefs.setAptoideLookupEnabled(true)
        prefs.setApkMirrorLookupEnabled(true)
        prefs.setApkPureLookupEnabled(true)
        val repos = FdroidRepoPreferences(activity)
        listOf("official", "archive", "izzy", "guardian", "calyx").forEach { id ->
            repos.setRepoEnabled(id, true)
        }
        FdroidRepoCatalog.defaults().filter { it.id !in MAIN_REPOS }.forEach { repo ->
            repos.setRepoEnabled(repo.id, false)
        }
        Log.i("DevPulse", "refresh launch all sources")
    }

    private val MAIN_REPOS = setOf("official", "archive", "izzy", "guardian", "calyx")
}
