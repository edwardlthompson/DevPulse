package dev.foss.goldenpath.index.apkpure

fun interface ApkPureBatchFetcher {
    fun fetch(packageNames: List<String>): Result<String>
}

object ApkPureCachePolicy {
    const val TTL_MS = 24 * 60 * 60 * 1000L
}

object ApkPureFetchPolicy {
    const val CONNECT_TIMEOUT_MS = 15_000
    const val READ_TIMEOUT_MS = 20_000
    const val CHUNK = 200
    const val USER_AGENT = "DevPulse/0.22 (https://github.com/edwardlthompson/DevPulse)"
    const val UPDATE_URL = "https://tapi.pureapk.com/v3/get_app_update"
}
