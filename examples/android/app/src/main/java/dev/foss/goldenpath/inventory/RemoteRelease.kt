package dev.foss.goldenpath.inventory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RemoteReleasedSource {
    Play,
    Fdroid,
    ExtraRepo,
    Izzy,
    Guardian,
    Calyx,
    Archive,
    Aptoide,
    ApkMirror,
    ApkPure,
    Forge,
    None,
}

data class RemoteDate(
    val ms: Long,
    val source: RemoteReleasedSource,
)

data class RemoteReleaseOffer(
    val source: RemoteReleasedSource,
    val ms: Long? = null,
    val versionName: String? = null,
    val pageUrl: String? = null,
    val listed: Boolean = true,
    val known: Boolean = true,
)

data class UpdateLink(
    val source: RemoteReleasedSource,
    val url: String? = null,
    val versionName: String? = null,
    val releasedAtMs: Long? = null,
    val listed: Boolean = true,
    val known: Boolean = true,
)

data class RemoteReleasePick(
    val ms: Long?,
    val source: RemoteReleasedSource,
    val versionName: String? = null,
    val pageUrl: String? = null,
    val versionSource: RemoteReleasedSource = RemoteReleasedSource.None,
    val offers: List<RemoteReleaseOffer> = emptyList(),
)

object RemoteRelease {
    fun pick(vararg dates: RemoteDate?): RemoteReleasePick {
        val usable = dates.mapNotNull { date ->
            date?.takeIf { it.ms > 0L }
        }
        val best = usable.maxByOrNull { it.ms } ?: return RemoteReleasePick(null, RemoteReleasedSource.None)
        return RemoteReleasePick(best.ms, best.source)
    }

    fun ageMs(app: InstalledApp): Long? = app.remoteReleasedAtMs ?: app.installedAtMs

    fun apply(app: InstalledApp, pick: RemoteReleasePick): InstalledApp {
        val listings = UpdateInventory.listingsFor(pick)
        return app.copy(
            remoteReleasedAtMs = pick.ms,
            remoteReleasedSource = pick.source,
            remoteVersionName = pick.versionName,
            remoteVersionSource = pick.versionSource,
            latestListings = listings,
            updateLinks = UpdateInventory.linksFor(app.versionName, pick),
        )
    }
}

object RemoteReleaseMemory {
    @Volatile
    var byPackage: Map<String, RemoteReleasePick> = emptyMap()
        private set

    private val lock = Any()
    private val revisionState = MutableStateFlow(0)
    val revision: StateFlow<Int> = revisionState.asStateFlow()

    fun putAll(updates: Map<String, RemoteReleasePick>) {
        if (updates.isEmpty()) return
        synchronized(lock) {
            byPackage = byPackage + updates
            persist?.save(byPackage)
            revisionState.value += 1
        }
    }

    fun hydrate(store: RemoteReleaseStore) {
        synchronized(lock) {
            persist = store
            if (byPackage.isNotEmpty()) return
            val loaded = store.load()
            if (loaded.isEmpty()) return
            byPackage = loaded
            revisionState.value += 1
        }
    }

    fun merge(app: InstalledApp): InstalledApp {
        val pick = byPackage[app.packageName]
        val dated = if (pick != null) RemoteRelease.apply(app, pick) else app
        return dated.copy(origin = AppOriginResolver.refine(dated.origin, pick?.source))
    }

    fun clear() {
        synchronized(lock) {
            byPackage = emptyMap()
            revisionState.value += 1
        }
    }

    private var persist: RemoteReleaseStore? = null
}
