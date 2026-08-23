package dev.foss.goldenpath.index.play

enum class PlayLookupStatus {
    Ok,
    UnknownCheckManually,
}

data class PlayLookup(
    val updatedOnMs: Long?,
    val publishedVersion: String?,
    val status: PlayLookupStatus,
    val developerUrl: String? = null,
)

object PlayCachePolicy {
    const val TTL_MS = 24 * 60 * 60 * 1000L
    const val MISS_TTL_MS = 7 * 24 * 60 * 60 * 1000L

    fun isFresh(fetchedAtMs: Long, nowMs: Long): Boolean =
        nowMs - fetchedAtMs in 0 until TTL_MS
}

object PlayFetchPolicy {
    const val MIN_INTERVAL_MS = 1_500L
    const val CONNECT_TIMEOUT_MS = 8_000
    const val READ_TIMEOUT_MS = 12_000
    const val MAX_BODY_BYTES = 1_250_000
    const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.186 Mobile Safari/537.36"
    const val ACCEPT_LANGUAGE = "en-US,en;q=0.9"
    const val ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
}

data class PlayPageResponse(
    val code: Int,
    val body: String,
    val retryAfterSec: Long? = null,
)

fun interface PlayPageFetcher {
    fun fetch(packageName: String): Result<String>
}

fun interface PlayPageClient {
    fun get(packageName: String): PlayPageResponse
}
