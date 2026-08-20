package dev.foss.goldenpath.index.aptoide

import dev.foss.goldenpath.inventory.InstalledDateResolver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object AptoideMetaParser {
    private val dataBlock = Regex(""""data"\s*:\s*\{""")
    private val updated = Regex(""""updated"\s*:\s*"([^"]+)"""")
    private val modified = Regex(""""modified"\s*:\s*"([^"]+)"""")
    private val added = Regex(""""added"\s*:\s*"([^"]+)"""")
    private val vername = Regex(""""vername"\s*:\s*"([^"]+)"""")
    private val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    fun parse(json: String, nowMs: Long = System.currentTimeMillis()): AptoideLookup {
        if (!dataBlock.containsMatchIn(json)) {
            return unknown()
        }
        val version = vername.find(json)?.groupValues?.get(1)?.trim()?.ifEmpty { null }
        val stamp = firstStamp(
            updated.find(json)?.groupValues?.get(1),
            modified.find(json)?.groupValues?.get(1),
            added.find(json)?.groupValues?.get(1),
        )
        val ms = stamp?.takeIf { InstalledDateResolver.isPlausible(it, nowMs) }
        return if (ms == null) unknown(version) else AptoideLookup(ms, version, AptoideLookupStatus.Ok)
    }

    private fun unknown(version: String? = null) = AptoideLookup(
        updatedOnMs = null,
        publishedVersion = version,
        status = AptoideLookupStatus.UnknownCheckManually,
    )

    private fun firstStamp(vararg raw: String?): Long? =
        raw.mapNotNull { it?.trim()?.ifEmpty { null } }.firstNotNullOfOrNull { parseStamp(it) }

    private fun parseStamp(raw: String): Long? {
        val iso = raw.replace(' ', 'T')
        return try {
            if (iso.length >= 19) {
                LocalDateTime.parse(iso.take(19), dateTime).toInstant(ZoneOffset.UTC).toEpochMilli()
            } else {
                LocalDate.parse(iso.take(10)).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            }
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
