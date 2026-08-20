package dev.foss.goldenpath.index.apkmirror

import dev.foss.goldenpath.inventory.InstalledDateResolver
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object ApkMirrorMetaParser {
    private val pname = Regex(""""pname"\s*:\s*"([^"]+)"""")
    private val exists = Regex(""""exists"\s*:\s*(true|false)""")
    private val version = Regex(""""version"\s*:\s*"([^"]+)"""")
    private val publish = Regex(""""publish_date"\s*:\s*"([^"]+)"""")
    private val link = Regex(""""link"\s*:\s*"([^"]+)"""")
    private val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    fun parseMany(json: String, nowMs: Long): Map<String, RemoteReleaseOffer> {
        if (json.isBlank()) return emptyMap()
        val chunks = json.split(Regex("\\}\\s*,\\s*\\{"))
        return chunks.mapNotNull { chunk -> parseOne(chunk, nowMs) }.associateBy { it.first }.mapValues { it.value.second }
    }

    private fun parseOne(chunk: String, nowMs: Long): Pair<String, RemoteReleaseOffer>? {
        val pkg = pname.find(chunk)?.groupValues?.get(1)?.trim().orEmpty()
        if (pkg.isEmpty()) return null
        val listed = exists.find(chunk)?.groupValues?.get(1) == "true"
        val ver = version.find(chunk)?.groupValues?.get(1)?.trim()?.ifEmpty { null }
        val path = link.find(chunk)?.groupValues?.get(1)?.trim()?.ifEmpty { null }
        val ms = publish.find(chunk)?.groupValues?.get(1)?.let { parseStamp(it) }
            ?.takeIf { InstalledDateResolver.isPlausible(it, nowMs) }
        return pkg to RemoteReleaseOffer(
            source = RemoteReleasedSource.ApkMirror,
            ms = ms,
            versionName = ver,
            pageUrl = if (listed) ApkMirrorLink.webPage(pkg, path) else null,
            listed = listed,
            known = true,
        )
    }

    private fun parseStamp(raw: String): Long? {
        val iso = raw.trim().replace(' ', 'T')
        if (iso.isEmpty()) return null
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
