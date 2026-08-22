package dev.foss.goldenpath.index.apkmirror

fun interface ApkMirrorBatchFetcher {
    fun fetch(packageNames: List<String>): Result<String>
}

object ApkMirrorCachePolicy {
    const val TTL_MS = 24 * 60 * 60 * 1000L
}

object ApkMirrorFetchPolicy {
    const val CONNECT_TIMEOUT_MS = 15_000
    const val READ_TIMEOUT_MS = 20_000
    const val CHUNK = 100
    const val PARALLEL = 4
    const val USER_AGENT = "DevPulse/0.22 (https://github.com/edwardlthompson/DevPulse)"
    const val EXISTS_URL = "https://www.apkmirror.com/wp-json/apkm/v1/app_exists/"
    /** Public APKUpdater partner header (FOSS-published). Not a user secret. */
    const val AUTH = "Basic YXBpLWFwa3VwZGF0ZXI6cm01cmNmcnVVakt5MDRzTXB5TVBKWFc4"
}
