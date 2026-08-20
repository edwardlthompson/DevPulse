package dev.foss.goldenpath.index.play

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object PlayHtmlParser {
    private val datePublished = Regex(
        """itemprop="datePublished"[^>]*content="([^"]+)"""",
        RegexOption.IGNORE_CASE,
    )
    private val softwareVersion = Regex(
        """itemprop="softwareVersion"[^>]*content="([^"]+)"""",
        RegexOption.IGNORE_CASE,
    )
    private val jsonDate = Regex(""""datePublished"\s*:\s*"([^"]+)"""")
    private val jsonVersion = Regex(""""softwareVersion"\s*:\s*"([^"]+)"""")
    private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE
    private val botHints = listOf(
        "before you continue",
        "consent and cookies",
        "unusual traffic",
    )
    private val listedHints = listOf(
        "apps on google play",
        "softwareapplication",
        "itemprop=\"name\"",
        "itemprop=\"applicationcategory\"",
    )

    fun parse(html: String): PlayLookup {
        val dateRaw = datePublished.find(html)?.groupValues?.get(1)?.trim()
            ?: jsonDate.find(html)?.groupValues?.get(1)?.trim()
        val version = softwareVersion.find(html)?.groupValues?.get(1)?.trim()?.ifEmpty { null }
            ?: jsonVersion.find(html)?.groupValues?.get(1)?.trim()?.ifEmpty { null }
        val updatedOnMs = dateRaw?.let { parseIsoDate(it) }
        return if (updatedOnMs == null && version == null) {
            PlayLookup(null, null, PlayLookupStatus.UnknownCheckManually)
        } else if (updatedOnMs == null) {
            PlayLookup(null, version, PlayLookupStatus.UnknownCheckManually)
        } else {
            PlayLookup(updatedOnMs, version, PlayLookupStatus.Ok)
        }
    }

    fun looksListed(html: String): Boolean {
        if (html.isBlank()) return false
        val lower = html.lowercase()
        if (botHints.any { it in lower }) return false
        return listedHints.any { it in lower }
    }

    private fun parseIsoDate(raw: String): Long? {
        val datePart = raw.take(10)
        return try {
            LocalDate.parse(datePart, isoDate)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
