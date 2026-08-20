package dev.foss.goldenpath.index.apkpure

import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifact
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import dev.foss.goldenpath.inventory.UpdateNotes
import dev.foss.goldenpath.inventory.UpdateNotesMemory
import dev.foss.goldenpath.inventory.UpdateNotesText

object ApkPureMetaParser {
    private val pkg = Regex(""""package_name"\s*:\s*"([^"]+)"""")
    private val version = Regex(""""version_name"\s*:\s*"([^"]+)"""")
    private val versionCode = Regex(""""version_code"\s*:\s*(-?\d+)""")
    private val assetUrl = Regex(""""asset"\s*:\s*\{[^}]*"url"\s*:\s*"(https?://[^"]+)"""")
    private val whatsNew = Regex(""""whatsnew"\s*:\s*"((?:\\.|[^"\\])*)"""")

    fun parseMany(json: String): Map<String, RemoteReleaseOffer> {
        if (json.isBlank() || !json.contains("app_update_response")) return emptyMap()
        val chunks = json.split(Regex("\\}\\s*,\\s*\\{"))
        return chunks.mapNotNull(::parseOne).associateBy { it.first }.mapValues { it.value.second }
    }

    private fun parseOne(chunk: String): Pair<String, RemoteReleaseOffer>? {
        val name = pkg.find(chunk)?.groupValues?.get(1)?.trim().orEmpty()
        if (name.isEmpty()) return null
        val ver = version.find(chunk)?.groupValues?.get(1)?.trim()?.ifEmpty { null }
        remember(name, chunk, ver)
        return name to RemoteReleaseOffer(
            source = RemoteReleasedSource.ApkPure,
            versionName = ver,
            pageUrl = ApkPureLink.webPage(name),
            listed = true,
            known = true,
        )
    }

    private fun remember(name: String, chunk: String, ver: String?) {
        UpdateNotesText.take(whatsNew.find(chunk)?.groupValues?.get(1))?.let {
            UpdateNotesMemory.putIfAbsent(name, UpdateNotes(it, RemoteReleasedSource.ApkPure))
        }
        val url = assetUrl.find(chunk)?.groupValues?.get(1) ?: return
        UpdateArtifactMemory.add(
            UpdateArtifact(
                packageName = name,
                source = RemoteReleasedSource.ApkPure,
                downloadUrl = url,
                versionName = ver,
                versionCode = versionCode.find(chunk)?.groupValues?.get(1)?.toLongOrNull(),
            ),
        )
    }
}
