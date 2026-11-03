package dev.foss.goldenpath.index

/** Shared per-source HTTP budget for index and listing probes. */
object SourceFetchPolicy {
    const val TIMEOUT_MS = 20_000
    const val CONNECT_TIMEOUT_MS = TIMEOUT_MS
    const val READ_TIMEOUT_MS = TIMEOUT_MS
    const val INDEX_CACHE_TTL_MS = 12 * 60 * 60 * 1000L
}
