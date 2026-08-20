package dev.foss.goldenpath.inventory

object RemoteReleaseRollup {
    fun from(offers: List<RemoteReleaseOffer>): RemoteReleasePick {
        val usable = offers.filter { it.listed }
        if (usable.isEmpty()) {
            return RemoteReleasePick(null, RemoteReleasedSource.None, offers = offers)
        }
        val newest = usable.filter { (it.ms ?: 0L) > 0L }.maxByOrNull { it.ms ?: 0L }
        val versions = usable.mapNotNull { it.versionName?.trim()?.takeIf(String::isNotEmpty) }
        val highest = versions.maxWithOrNull { left, right -> VersionCompare.compare(left, right) }
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
            pageUrl = versionOffer?.pageUrl,
            versionSource = versionOffer?.source ?: RemoteReleasedSource.None,
            offers = offers,
        )
    }
}
