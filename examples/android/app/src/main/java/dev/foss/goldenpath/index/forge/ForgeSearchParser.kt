package dev.foss.goldenpath.index.forge

object ForgeSearchParser {
    private val item = Regex("""\{[^{}]*\}""")
    private val ownerRepo = Regex(""""ownerRepo"\s*:\s*"([^"]+)"""")
    private val packageId = Regex(""""packageId"\s*:\s*"([^"]+)"""")
    private val title = Regex(""""title"\s*:\s*"([^"]+)"""")
    private val host = Regex(""""host"\s*:\s*"([^"]+)"""")
    private val commit = Regex(""""latestCommitMs"\s*:\s*(\d+)""")
    private val release = Regex(""""latestReleaseMs"\s*:\s*(\d+)""")
    private val archived = Regex(""""archived"\s*:\s*(true|false)""")

    fun parse(json: String): List<ForgeCandidate> =
        item.findAll(json).mapNotNull { match ->
            val body = match.value
            val repo = ownerRepo.find(body)?.groupValues?.get(1) ?: return@mapNotNull null
            ForgeCandidate(
                host = runCatching {
                    ForgeHost.valueOf(host.find(body)?.groupValues?.get(1) ?: "GitHub")
                }.getOrDefault(ForgeHost.GitHub),
                ownerRepo = repo,
                packageId = packageId.find(body)?.groupValues?.get(1),
                title = title.find(body)?.groupValues?.get(1) ?: repo,
                latestCommitMs = commit.find(body)?.groupValues?.get(1)?.toLongOrNull(),
                latestReleaseMs = release.find(body)?.groupValues?.get(1)?.toLongOrNull(),
                archived = archived.find(body)?.groupValues?.get(1) == "true",
            )
        }.toList()
}

object ForgeSourceLinks {
    fun fromRecords(fdroidSource: String?, playDescriptionUrl: String?): List<String> =
        listOfNotNull(fdroidSource?.trim()?.ifEmpty { null }, playDescriptionUrl?.trim()?.ifEmpty { null })
}
