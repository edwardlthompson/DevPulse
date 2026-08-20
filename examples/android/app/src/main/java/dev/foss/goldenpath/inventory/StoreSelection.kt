package dev.foss.goldenpath.inventory

object StoreSelection {
    fun sources(
        play: Boolean,
        aptoide: Boolean,
        forge: Boolean,
        repoIds: Set<String>,
    ): Set<RemoteReleasedSource> = buildSet {
        if (play) add(RemoteReleasedSource.Play)
        if (aptoide) add(RemoteReleasedSource.Aptoide)
        if (forge) add(RemoteReleasedSource.Forge)
        repoIds.forEach { add(ListingChannels.sourceForRepo(it)) }
    }

    fun visible(listings: List<UpdateLink>, enabled: Set<RemoteReleasedSource>): List<UpdateLink> =
        rows(listings, enabled)

    fun rows(listings: List<UpdateLink>, enabled: Set<RemoteReleasedSource>): List<UpdateLink> {
        val bySource = listings.filter { it.source in enabled }.associateBy { it.source }
        return enabled
            .map { source ->
                bySource[source] ?: UpdateLink(source = source, listed = false, known = false)
            }
            .sortedBy { UpdateLinkRank.rank(it.source) }
    }
}
