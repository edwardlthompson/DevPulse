package dev.foss.goldenpath.inventory

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
    val versionCode: Long? = null,
    val listed: Boolean = true,
    val known: Boolean = true,
    val fetchedAtMs: Long? = null,
    val miss: ListingMiss? = null,
)

data class UpdateLink(
    val source: RemoteReleasedSource,
    val url: String? = null,
    val versionName: String? = null,
    val releasedAtMs: Long? = null,
    val listed: Boolean = true,
    val known: Boolean = true,
    val miss: ListingMiss? = null,
    val sizeBytes: Long? = null,
    val antiFeatures: List<String> = emptyList(),
    val minSdk: Int? = null,
    val nativeCodes: Set<String> = emptySet(),
    val versionCode: Long? = null,
)

data class RemoteReleasePick(
    val ms: Long?,
    val source: RemoteReleasedSource,
    val versionName: String? = null,
    val pageUrl: String? = null,
    val versionSource: RemoteReleasedSource = RemoteReleasedSource.None,
    val offers: List<RemoteReleaseOffer> = emptyList(),
    val versionCode: Long? = null,
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

    fun lastReleaseMs(app: InstalledApp): Long? {
        val ms = app.remoteReleasedAtMs
        if (ms == null || app.remoteReleasedSource == RemoteReleasedSource.None) return null
        return ms
    }

    fun apply(app: InstalledApp, pick: RemoteReleasePick): InstalledApp {
        val listings = UpdateInventory.listingsFor(pick, app.packageName)
        return app.copy(
            remoteReleasedAtMs = pick.ms,
            remoteReleasedSource = pick.source,
            remoteVersionName = pick.versionName,
            remoteVersionCode = pick.versionCode,
            remoteVersionSource = pick.versionSource,
            latestListings = listings,
            updateLinks = UpdateInventory.linksFor(app.versionName, pick),
        )
    }
}
