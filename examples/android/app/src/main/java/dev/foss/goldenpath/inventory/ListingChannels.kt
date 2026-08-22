package dev.foss.goldenpath.inventory

object ListingChannels {
    val STANDARD = setOf(
        RemoteReleasedSource.Play,
        RemoteReleasedSource.Fdroid,
        RemoteReleasedSource.Archive,
        RemoteReleasedSource.Izzy,
        RemoteReleasedSource.Guardian,
        RemoteReleasedSource.Calyx,
        RemoteReleasedSource.Aptoide,
        RemoteReleasedSource.ApkMirror,
        RemoteReleasedSource.ApkPure,
        RemoteReleasedSource.Forge,
    )

    fun sourceForRepo(repoId: String): RemoteReleasedSource = when (repoId) {
        "official" -> RemoteReleasedSource.Fdroid
        "archive" -> RemoteReleasedSource.Archive
        "izzy" -> RemoteReleasedSource.Izzy
        "guardian" -> RemoteReleasedSource.Guardian
        "calyx" -> RemoteReleasedSource.Calyx
        else -> RemoteReleasedSource.ExtraRepo
    }

    fun repoId(source: RemoteReleasedSource): String? = when (source) {
        RemoteReleasedSource.Fdroid -> "official"
        RemoteReleasedSource.Archive -> "archive"
        RemoteReleasedSource.Izzy -> "izzy"
        RemoteReleasedSource.Guardian -> "guardian"
        RemoteReleasedSource.Calyx -> "calyx"
        else -> null
    }

    fun complete(
        offers: List<RemoteReleaseOffer>,
        searched: Set<RemoteReleasedSource>,
    ): List<RemoteReleaseOffer> {
        val present = offers.map { it.source }.toSet()
        val pads = searched
            .filter { it != RemoteReleasedSource.None && it !in present }
            .map { RemoteReleaseOffer(source = it, listed = false, known = false) }
        return offers + pads
    }

    fun relabel(offer: RemoteReleaseOffer): RemoteReleaseOffer {
        val url = offer.pageUrl.orEmpty()
        val source = when {
            url.contains("apt.izzysoft.de") -> RemoteReleasedSource.Izzy
            url.contains("guardianproject.info") -> RemoteReleasedSource.Guardian
            url.contains("calyxos.gitlab.io") || url.contains("calyx") && offer.source == RemoteReleasedSource.ExtraRepo ->
                RemoteReleasedSource.Calyx
            else -> offer.source
        }
        return if (source == offer.source) offer else offer.copy(source = source)
    }

    fun isIndexedRepo(source: RemoteReleasedSource): Boolean = when (source) {
        RemoteReleasedSource.Fdroid,
        RemoteReleasedSource.ExtraRepo,
        RemoteReleasedSource.Izzy,
        RemoteReleasedSource.Guardian,
        RemoteReleasedSource.Calyx,
        RemoteReleasedSource.Archive,
        -> true
        else -> false
    }
}
