package dev.foss.goldenpath.inventory

import java.util.concurrent.ConcurrentHashMap

data class ListingExtra(
    val sizeBytes: Long? = null,
    val antiFeatures: List<String> = emptyList(),
    val minSdk: Int? = null,
    val nativeCodes: Set<String> = emptySet(),
)

object ListingExtraBook {
    private val rows = ConcurrentHashMap<String, ListingExtra>()

    fun put(packageName: String, source: RemoteReleasedSource, extra: ListingExtra) {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || source == RemoteReleasedSource.None) return
        if (extra.sizeBytes == null && extra.antiFeatures.isEmpty() && extra.minSdk == null && extra.nativeCodes.isEmpty()) return
        rows[key(pkg, source)] = extra
    }

    fun get(packageName: String, source: RemoteReleasedSource): ListingExtra? =
        rows[key(packageName.trim(), source)]

    fun clear() {
        rows.clear()
    }

    private fun key(packageName: String, source: RemoteReleasedSource): String =
        "$packageName\t${source.name}"
}
