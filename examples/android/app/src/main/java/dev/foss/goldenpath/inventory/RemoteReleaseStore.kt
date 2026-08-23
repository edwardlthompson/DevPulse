package dev.foss.goldenpath.inventory

import java.io.File

interface RemoteReleaseStore {
    fun load(): Map<String, RemoteReleasePick>
    fun save(all: Map<String, RemoteReleasePick>)
}

object RemoteReleaseCodec {
    fun encode(all: Map<String, RemoteReleasePick>): String = buildString {
        all.forEach { (pkg, pick) ->
            rowsOf(pick).forEach { offer ->
                append(esc(pkg)).append('\t')
                append(offer.source.name).append('\t')
                append(offer.ms ?: "").append('\t')
                append(esc(offer.versionName)).append('\t')
                append(esc(offer.pageUrl)).append('\t')
                append(listingFlag(offer)).append('\t')
                append(offer.fetchedAtMs ?: "").append('\n')
            }
        }
    }

    fun decode(raw: String): Map<String, RemoteReleasePick> {
        if (raw.isBlank()) return emptyMap()
        val grouped = linkedMapOf<String, MutableList<RemoteReleaseOffer>>()
        raw.lineSequence().forEach { line ->
            val parsed = parseLine(line) ?: return@forEach
            grouped.getOrPut(parsed.first) { mutableListOf() }.add(parsed.second)
        }
        return grouped.mapValues { RemoteReleaseRollup.from(it.value) }
    }

    private fun rowsOf(pick: RemoteReleasePick): List<RemoteReleaseOffer> {
        if (pick.offers.isNotEmpty()) return pick.offers
        if (pick.source == RemoteReleasedSource.None && pick.ms == null && pick.versionName == null) {
            return emptyList()
        }
        return listOf(
            RemoteReleaseOffer(pick.source, pick.ms, pick.versionName, pick.pageUrl, listed = true),
        )
    }

    private fun parseLine(line: String): Pair<String, RemoteReleaseOffer>? {
        val cols = line.split('\t')
        return when {
            cols.size >= 6 -> newOffer(cols)
            cols.size == 5 -> oldOffer(cols)
            else -> null
        }
    }

    private fun newOffer(cols: List<String>): Pair<String, RemoteReleaseOffer>? {
        val pkg = unesc(cols[0])
        if (pkg.isEmpty()) return null
        val source = runCatching { RemoteReleasedSource.valueOf(cols[1]) }
            .getOrDefault(RemoteReleasedSource.None)
        val flag = cols[5].lowercase()
        return pkg to RemoteReleaseOffer(
            source = source,
            ms = cols[2].toLongOrNull(),
            versionName = unesc(cols[3]).ifEmpty { null },
            pageUrl = unesc(cols[4]).ifEmpty { null },
            listed = flag == "1" || flag == "true",
            known = flag !in setOf("?", "unknown", "403", "parse"),
            fetchedAtMs = cols.getOrNull(6)?.toLongOrNull(),
            miss = missOf(flag),
        )
    }

    private fun oldOffer(cols: List<String>): Pair<String, RemoteReleaseOffer>? {
        val pkg = unesc(cols[0])
        if (pkg.isEmpty()) return null
        val source = runCatching { RemoteReleasedSource.valueOf(cols[2]) }
            .getOrDefault(RemoteReleasedSource.None)
        return pkg to RemoteReleaseOffer(
            source = source,
            ms = cols[1].toLongOrNull(),
            versionName = unesc(cols[3]).ifEmpty { null },
            pageUrl = unesc(cols[4]).ifEmpty { null },
            listed = true,
        )
    }

    private fun listingFlag(offer: RemoteReleaseOffer): String = when {
        offer.listed -> "1"
        offer.miss == ListingMiss.Forbidden -> "403"
        offer.miss == ListingMiss.Parse -> "parse"
        !offer.known -> "?"
        else -> "0"
    }

    private fun missOf(flag: String): ListingMiss? = when (flag) {
        "403" -> ListingMiss.Forbidden
        "parse" -> ListingMiss.Parse
        "0" -> ListingMiss.Never
        else -> null
    }

    private fun esc(value: String?): String = (value ?: "").replace("\t", " ").replace("\n", " ")

    private fun unesc(value: String): String = value.trim()
}

class FileRemoteReleaseStore(private val file: File) : RemoteReleaseStore {
    override fun load(): Map<String, RemoteReleasePick> =
        runCatching { RemoteReleaseCodec.decode(file.readText(Charsets.UTF_8)) }.getOrDefault(emptyMap())

    override fun save(all: Map<String, RemoteReleasePick>) {
        runCatching { file.writeText(RemoteReleaseCodec.encode(all), Charsets.UTF_8) }
    }
}
