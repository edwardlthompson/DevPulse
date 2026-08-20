package dev.foss.goldenpath.index.apkpure

import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource

object ApkPureMetaParser {
    private val pkg = Regex(""""package_name"\s*:\s*"([^"]+)"""")
    private val version = Regex(""""version_name"\s*:\s*"([^"]+)"""")

    fun parseMany(json: String): Map<String, RemoteReleaseOffer> {
        if (json.isBlank() || !json.contains("app_update_response")) return emptyMap()
        val chunks = json.split(Regex("\\}\\s*,\\s*\\{"))
        return chunks.mapNotNull(::parseOne).associateBy { it.first }.mapValues { it.value.second }
    }

    private fun parseOne(chunk: String): Pair<String, RemoteReleaseOffer>? {
        val name = pkg.find(chunk)?.groupValues?.get(1)?.trim().orEmpty()
        if (name.isEmpty()) return null
        val ver = version.find(chunk)?.groupValues?.get(1)?.trim()?.ifEmpty { null }
        return name to RemoteReleaseOffer(
            source = RemoteReleasedSource.ApkPure,
            versionName = ver,
            pageUrl = ApkPureLink.webPage(name),
            listed = true,
            known = true,
        )
    }
}
