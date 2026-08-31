package dev.foss.goldenpath.index.apkpure

import com.google.gson.JsonParser
import dev.foss.goldenpath.inventory.ApkDownloadUrl
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifact
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import dev.foss.goldenpath.inventory.UpdateNotes
import dev.foss.goldenpath.inventory.UpdateNotesMemory
import dev.foss.goldenpath.inventory.UpdateNotesText

object ApkPureMetaParser {
    fun parseMany(json: String): Map<String, RemoteReleaseOffer> {
        if (json.isBlank()) return emptyMap()
        return runCatching {
            val root = JsonParser.parseString(json).asJsonObject
            val list = root.getAsJsonArray("app_update_response") ?: return emptyMap()
            val result = mutableMapOf<String, RemoteReleaseOffer>()
            for (elem in list) {
                if (!elem.isJsonObject) continue
                val obj = elem.asJsonObject
                val name = obj.get("package_name")?.asString?.trim().orEmpty()
                if (name.isEmpty()) continue
                val ver = obj.get("version_name")?.asString?.trim()?.ifEmpty { null }
                val code = obj.get("version_code")?.asLong

                obj.get("whatsnew")?.asString?.let { rawNotes ->
                    UpdateNotesText.take(rawNotes)?.let {
                        UpdateNotesMemory.putIfAbsent(name, UpdateNotes(it, RemoteReleasedSource.ApkPure))
                    }
                }

                val assetObj = if (obj.has("asset") && obj.get("asset").isJsonObject) {
                    obj.getAsJsonObject("asset")
                } else null
                val rawUrl = assetObj?.get("url")?.asString?.trim().orEmpty()
                if (rawUrl.isNotEmpty()) {
                    val prefer = ApkDownloadUrl.jsonUrl(rawUrl).replace("/b/XAPK/", "/b/APK/", ignoreCase = true)
                    ApkDownloadUrl.httpsFile(prefer)?.let { url ->
                        UpdateArtifactMemory.add(
                            UpdateArtifact(
                                packageName = name,
                                source = RemoteReleasedSource.ApkPure,
                                downloadUrl = url,
                                versionName = ver,
                                versionCode = code,
                            ),
                        )
                    }
                }

                result[name] = RemoteReleaseOffer(
                    source = RemoteReleasedSource.ApkPure,
                    versionName = ver,
                    versionCode = code,
                    pageUrl = ApkPureLink.webPage(name),
                    listed = true,
                    known = true,
                )
            }
            result
        }.getOrDefault(emptyMap())
    }
}
