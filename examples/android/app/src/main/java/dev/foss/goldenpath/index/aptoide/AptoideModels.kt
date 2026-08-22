package dev.foss.goldenpath.index.aptoide

enum class AptoideLookupStatus {
    Ok,
    UnknownCheckManually,
}

data class AptoideLookup(
    val updatedOnMs: Long?,
    val publishedVersion: String?,
    val status: AptoideLookupStatus,
    val uname: String? = null,
    val fileUrl: String? = null,
)

object AptoideCachePolicy {
    const val TTL_MS = 24 * 60 * 60 * 1000L
    const val MISS_TTL_MS = 7 * 24 * 60 * 60 * 1000L

    fun isFresh(fetchedAtMs: Long, nowMs: Long): Boolean =
        nowMs - fetchedAtMs in 0 until TTL_MS
}

object AptoideFetchPolicy {
    const val MIN_INTERVAL_MS = 1_500L
    const val CONNECT_TIMEOUT_MS = 10_000
    const val READ_TIMEOUT_MS = 10_000
    const val USER_AGENT = "DevPulse/0.1 (https://github.com/edwardlthompson/DevPulse)"
    const val META_URL = "https://ws2.aptoide.com/api/7/app/getMeta"
    const val UPDATES_URL = "https://ws75.aptoide.com/api/7/listAppsUpdates"
    const val CHUNK = 100
}

data class AptoideApkRef(
    val packageName: String,
    val signature: String? = null,
    val versionCode: Long = 0L,
)

fun interface AptoideMetaFetcher {
    fun fetch(packageName: String): Result<String>
}

fun interface AptoideUpdatesFetcher {
    fun fetch(apks: List<AptoideApkRef>): Result<String>
}
