package dev.foss.goldenpath.inventory

enum class AppVersionState {
    Current,
    Newer,
    Rollback,
}

data class AppVersionItem(
    val versionName: String,
    val versionCode: Long? = null,
    val releasedAtMs: Long? = null,
    val source: RemoteReleasedSource,
    val downloadUrl: String? = null,
    val state: AppVersionState,
)
