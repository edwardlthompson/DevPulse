package dev.foss.goldenpath.index.play

enum class PlayPresence {
    Listed,
    Missing,
    Unknown,
}

object PlayListing {
    private val missingHints = listOf(
        "the requested url was not found",
        "we're sorry, the requested url was not found",
        "item not found on this server",
    )

    fun of(httpCode: Int, body: String, lookup: PlayLookup): PlayPresence {
        if (httpCode == 404 || httpCode == 410) return PlayPresence.Missing
        if (httpCode !in 200..299) return PlayPresence.Unknown
        if (lookup.updatedOnMs != null || !lookup.publishedVersion.isNullOrBlank()) {
            return PlayPresence.Listed
        }
        if (PlayHtmlParser.looksListed(body)) return PlayPresence.Listed
        val lower = body.lowercase()
        if (missingHints.any { it in lower }) return PlayPresence.Missing
        return PlayPresence.Unknown
    }
}
