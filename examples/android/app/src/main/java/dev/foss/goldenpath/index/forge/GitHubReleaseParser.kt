package dev.foss.goldenpath.index.forge

data class GitHubReleaseRecord(
    val publishedAtMs: Long?,
    val haystack: String,
)

object GitHubReleaseParser {
    private val name = Regex(""""name"\s*:\s*"([^"]*)"""")
    private val tagName = Regex(""""tag_name"\s*:\s*"([^"]*)"""")
    private val body = Regex(""""body"\s*:\s*"([^"]*)"""")
    private val published = Regex(""""published_at"\s*:\s*"([^"]*)"""")

    fun parse(json: String): List<GitHubReleaseRecord> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]" || trimmed == "{}") return emptyList()
        val inner = trimmed.removePrefix("[").removeSuffix("]").trim()
        if (inner.isEmpty()) return emptyList()
        return inner.split(Regex("\\}\\s*,\\s*\\{")).map { chunk ->
            val names = name.findAll(chunk).map { it.groupValues[1] }
            val bits = names + listOf(
                tagName.find(chunk)?.groupValues?.get(1).orEmpty(),
                body.find(chunk)?.groupValues?.get(1).orEmpty(),
            )
            GitHubReleaseRecord(
                publishedAtMs = GitHubRepoParser.isoMs(published.find(chunk)?.groupValues?.get(1)),
                haystack = bits.joinToString("\n"),
            )
        }
    }

    fun firstWithPackage(packageName: String, json: String): GitHubReleaseRecord? =
        parse(json).firstOrNull { ForgePackageEvidence.inText(packageName, it.haystack) }
}
