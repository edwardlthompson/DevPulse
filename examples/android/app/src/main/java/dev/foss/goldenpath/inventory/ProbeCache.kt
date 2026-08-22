package dev.foss.goldenpath.inventory

object ProbeCache {
    fun fresh(
        packageName: String,
        source: RemoteReleasedSource,
        nowMs: Long,
        ttlMs: Long,
        missTtlMs: Long = ttlMs,
    ): RemoteReleaseOffer? {
        val offer = RemoteReleaseMemory.byPackage[packageName]?.offers
            ?.firstOrNull { it.source == source } ?: return null
        if (!offer.known) return null
        val fetched = offer.fetchedAtMs ?: return null
        val ttl = if (offer.listed) ttlMs else missTtlMs
        if (nowMs - fetched !in 0 until ttl) return null
        return offer
    }

    fun stamp(offer: RemoteReleaseOffer, nowMs: Long): RemoteReleaseOffer =
        if (offer.fetchedAtMs != null) offer else offer.copy(fetchedAtMs = nowMs)
}
