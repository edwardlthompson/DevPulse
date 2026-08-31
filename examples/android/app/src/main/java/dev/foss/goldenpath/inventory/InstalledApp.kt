package dev.foss.goldenpath.inventory

data class InstalledApp(
    val packageName: String,
    val label: String,
    val versionName: String?,
    val versionCode: Long,
    val lastUpdateTimeMs: Long,
    val firstInstallTimeMs: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val isSystemApp: Boolean,
    val origin: AppOrigin = AppOrigin.Unknown,
    val installedAtMs: Long? = null,
    val installedAtSource: InstalledDateSource = InstalledDateSource.Unknown,
    val remoteReleasedAtMs: Long? = null,
    val remoteReleasedSource: RemoteReleasedSource = RemoteReleasedSource.None,
    val remoteVersionName: String? = null,
    val remoteVersionCode: Long? = null,
    val remoteVersionSource: RemoteReleasedSource = RemoteReleasedSource.None,
    val latestListings: List<UpdateLink> = emptyList(),
    val updateLinks: List<UpdateLink> = emptyList(),
    val signingSha1: String? = null,
)
