package dev.foss.goldenpath.index.aptoide

object AptoideLookupCache {
    private val fetchedAtMs = mutableMapOf<String, Long>()
    private val lookups = mutableMapOf<String, AptoideLookup>()

    @Synchronized
    fun getFresh(packageName: String, nowMs: Long): AptoideLookup? {
        val fetched = fetchedAtMs[packageName] ?: return null
        if (!AptoideCachePolicy.isFresh(fetched, nowMs)) return null
        return lookups[packageName]
    }

    @Synchronized
    fun put(packageName: String, lookup: AptoideLookup, nowMs: Long) {
        fetchedAtMs[packageName] = nowMs
        lookups[packageName] = lookup
    }
}
