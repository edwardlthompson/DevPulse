package dev.foss.goldenpath.index.apkpure

import dev.foss.goldenpath.index.SourceFetchPolicy

fun interface ApkPureBatchFetcher {
    fun fetch(packageNames: List<String>): Result<String>
}

object ApkPureCachePolicy {
    const val TTL_MS = SourceFetchPolicy.INDEX_CACHE_TTL_MS
}

object ApkPureFetchPolicy {
    const val CONNECT_TIMEOUT_MS = SourceFetchPolicy.CONNECT_TIMEOUT_MS
    const val READ_TIMEOUT_MS = SourceFetchPolicy.READ_TIMEOUT_MS
    const val CHUNK = 200
    const val USER_AGENT = "DevPulse/0.22 (https://github.com/edwardlthompson/DevPulse)"
    const val UPDATE_URL = "https://tapi.pureapk.com/v3/get_app_update"
}
