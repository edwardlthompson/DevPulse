package dev.foss.goldenpath.inventory

object RemoteReleaseRollup {
    fun merge(existing: RemoteReleasePick?, update: RemoteReleasePick): RemoteReleasePick {
        if (existing == null) return update
        val into = existing.offers.toMutableList()
        update.offers.forEach { extra ->
            into.removeAll { it.source == extra.source }
            into += extra
        }
        return from(into)
    }

    fun from(offers: List<RemoteReleaseOffer>): RemoteReleasePick {
        val usable = offers.filter { it.listed }
        val recovered = offers.filter { !it.listed && it.known && (it.ms ?: 0L) > 0L }
            .maxByOrNull { it.ms ?: 0L }
        if (usable.isEmpty()) {
            return if (recovered == null) {
                RemoteReleasePick(null, RemoteReleasedSource.None, offers = offers)
            } else {
                RemoteReleasePick(
                    ms = recovered.ms,
                    source = recovered.source,
                    versionName = recovered.versionName,
                    pageUrl = recovered.pageUrl,
                    versionSource = recovered.source,
                    offers = offers,
                )
            }
        }
        val newest = usable.filter { (it.ms ?: 0L) > 0L }.maxByOrNull { it.ms ?: 0L }
        val versions = usable.mapNotNull { it.versionName?.trim()?.takeIf(String::isNotEmpty) }
        val highest = versions.maxWithOrNull { left, right -> VersionCompare.compare(left, right) }
        val highestCode = usable.mapNotNull { it.versionCode }.maxOrNull()
        val versionOffer = highest?.let { ver ->
            usable.filter { offer ->
                val name = offer.versionName?.trim().orEmpty()
                name.isNotEmpty() && VersionCompare.compare(name, ver) == 0
            }.minByOrNull { UpdateLinkRank.rank(it.source) }
        }
        return RemoteReleasePick(
            ms = newest?.ms,
            source = newest?.source ?: RemoteReleasedSource.None,
            versionName = highest,
            versionCode = highestCode,
            pageUrl = versionOffer?.pageUrl,
            versionSource = versionOffer?.source ?: RemoteReleasedSource.None,
            offers = offers,
        )
    }
}
