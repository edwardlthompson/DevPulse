package dev.foss.goldenpath.inventory

object InventorySourceFilter {
    val CHIPS = listOf(
        RemoteReleasedSource.Play,
        RemoteReleasedSource.Fdroid,
        RemoteReleasedSource.Archive,
        RemoteReleasedSource.Izzy,
        RemoteReleasedSource.Guardian,
        RemoteReleasedSource.Calyx,
        RemoteReleasedSource.Aptoide,
        RemoteReleasedSource.Forge,
    )

    fun decode(raw: String?, legacyGithub: Boolean = false): Set<RemoteReleasedSource> {
        val stored = raw.orEmpty().split(',').mapNotNull { token ->
            runCatching { RemoteReleasedSource.valueOf(token.trim()) }.getOrNull()
        }.filter { it in CHIPS }.toSet()
        return if (legacyGithub) stored + RemoteReleasedSource.Forge else stored
    }

    fun encode(sources: Set<RemoteReleasedSource>): String =
        CHIPS.filter { it in sources }.joinToString(",") { it.name }

    fun toggle(
        current: Set<RemoteReleasedSource>,
        source: RemoteReleasedSource,
    ): Set<RemoteReleasedSource> = if (source in current) current - source else current + source
}
