package dev.foss.goldenpath.index.fdroid

object FdroidIndexParser {
    private val lastUpdated = Regex(""""lastUpdated"\s*:\s*(\d+)""")
    private val sourceCode = Regex(""""sourceCode"\s*:\s*"([^"]+)"""")
    private val suggestedName = Regex(""""suggestedVersionName"\s*:\s*"([^"]+)"""")
    private val suggestedCode = Regex(""""suggestedVersionCode"\s*:\s*(\d+)""")

    fun parse(raw: String, repoId: String, wanted: Set<String> = emptySet()): List<FdroidAppRecord> =
        parse(raw.toByteArray(Charsets.UTF_8), repoId, wanted)

    fun parse(raw: ByteArray, repoId: String, wanted: Set<String> = emptySet()): List<FdroidAppRecord> {
        if (FdroidIndexBytes.indexOf(raw, "\"apps\"") < 0) return emptyList()
        val highest = if (wanted.isEmpty()) {
            FdroidPackageVersions.highestByPackage(FdroidIndexBytes.utf8(raw, 0, raw.size))
        } else {
            FdroidPackageVersions.highestFor(raw, wanted)
        }
        val apks = FdroidApkFiles.namesFor(raw, wanted)
        return extract(raw, repoId, wanted, highest, apks)
    }

    private fun extract(
        raw: ByteArray,
        repoId: String,
        wanted: Set<String>,
        highest: Map<String, String>,
        apks: Map<String, FdroidApkHint>,
    ): List<FdroidAppRecord> {
        val found = LinkedHashMap<String, FdroidAppRecord>()
        var from = 0
        while (wanted.isEmpty() || found.size < wanted.size) {
            val at = FdroidIndexBytes.indexOf(raw, "\"packageName\"", from)
            if (at < 0) break
            from = at + 13
            val name = FdroidIndexBytes.readJsonString(raw, from) ?: continue
            if (name.isEmpty() || '.' !in name) continue
            if (wanted.isNotEmpty() && (name !in wanted || name in found)) continue
            if (wanted.isEmpty() && name in found) continue
            found[name] = recordFrom(name, FdroidIndexBytes.objectSlice(raw, at), repoId, highest[name], apks[name])
        }
        return if (wanted.isEmpty()) found.values.toList() else wanted.mapNotNull { found[it] }
    }

    private fun recordFrom(
        name: String,
        chunk: String,
        repoId: String,
        highest: String?,
        hint: FdroidApkHint?,
    ): FdroidAppRecord {
        val suggested = suggestedName.find(chunk)?.groupValues?.get(1)?.trim()?.ifEmpty { null }
            ?: suggestedCode.find(chunk)?.groupValues?.get(1)
        return FdroidAppRecord(
            packageName = name,
            lastUpdatedMs = lastUpdated.find(chunk)?.groupValues?.get(1)?.toLongOrNull(),
            sourceCode = sourceCode.find(chunk)?.groupValues?.get(1)?.trim()?.ifEmpty { null },
            repoId = repoId,
            suggestedVersionName = highest ?: suggested,
            whatsNew = FdroidWhatsNew.parse(chunk),
            apkName = hint?.apkName,
            apkSha256 = hint?.sha256,
            nativeCodes = hint?.nativeCodes.orEmpty(),
        )
    }
}

object FdroidLookupEngine {
    fun lookup(packageName: String, records: List<FdroidAppRecord>): FdroidAppRecord? =
        records.firstOrNull { it.packageName == packageName }
}

object FdroidCachePolicy {
    const val TTL_MS = 3 * 24 * 60 * 60 * 1000L

    fun isFresh(fetchedAtMs: Long, nowMs: Long): Boolean =
        nowMs - fetchedAtMs in 0 until TTL_MS
}
