package dev.foss.goldenpath.index.fdroid

data class FdroidVersionRecord(
    val versionName: String,
    val versionCode: Long? = null,
    val apkName: String? = null,
    val sha256: String? = null,
    val sizeBytes: Long? = null,
    val addedMs: Long? = null,
    val minSdk: Int? = null,
)
