package dev.foss.goldenpath.index.aptoide

object AptoideUpdatesParser {
    private val pkg = Regex(""""package"\s*:\s*"([^"]+)"""")

    fun parseMany(json: String, nowMs: Long): Map<String, AptoideLookup> {
        if (json.isBlank() || !json.contains("\"list\"")) return emptyMap()
        return json.split(Regex("\\}\\s*,\\s*\\{")).mapNotNull { chunk ->
            val name = pkg.find(chunk)?.groupValues?.get(1)?.trim().orEmpty()
            if (name.isEmpty()) return@mapNotNull null
            val body = if (chunk.trimStart().startsWith("{")) "{\"data\":$chunk}" else "{\"data\":{$chunk}}"
            val lookup = AptoideMetaParser.parse(body, nowMs)
            if (lookup.status != AptoideLookupStatus.Ok) return@mapNotNull null
            name to lookup
        }.toMap()
    }
}
