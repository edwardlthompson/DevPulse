package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.UpdateNotesText

data class GitHubReleaseRecord(
    val publishedAtMs: Long?,
    val haystack: String,
    val notes: String? = null,
    val apkUrl: String? = null,
    val prerelease: Boolean = false,
    val apkUrls: List<String> = emptyList(),
    val versionName: String? = null,
)

object GitHubReleaseParser {
    private val name = Regex(""""name"\s*:\s*"([^"]*)"""")
    private val tagName = Regex(""""tag_name"\s*:\s*"([^"]*)"""")
    private val body = Regex(""""body"\s*:\s*"((?:\\.|[^"\\])*)"""")
    private val published = Regex(""""published_at"\s*:\s*"([^"]*)"""")
    private val apkAsset = Regex(""""browser_download_url"\s*:\s*"(https://[^"]+\.apk)"""", RegexOption.IGNORE_CASE)
    private val prerelease = Regex(""""prerelease"\s*:\s*(true|false)""")

    fun parse(json: String): List<GitHubReleaseRecord> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]" || trimmed == "{}") return emptyList()
        val inner = trimmed.removePrefix("[").removeSuffix("]").trim()
        if (inner.isEmpty()) return emptyList()
        return inner.split(Regex("\\}\\s*,\\s*\\{")).map { chunk ->
            val rawBody = body.find(chunk)?.groupValues?.get(1)
            val names = name.findAll(chunk).map { it.groupValues[1] }.toList()
            val bits = names + listOf(
                tagName.find(chunk)?.groupValues?.get(1).orEmpty(),
                rawBody.orEmpty(),
            )
            val urls = apkAsset.findAll(chunk).map { it.groupValues[1] }.toList()
            val tag = tagName.find(chunk)?.groupValues?.get(1)?.trim().orEmpty()
            GitHubReleaseRecord(
                publishedAtMs = GitHubRepoParser.isoMs(published.find(chunk)?.groupValues?.get(1)),
                haystack = bits.joinToString("\n"),
                notes = UpdateNotesText.take(rawBody),
                apkUrl = urls.firstOrNull(),
                prerelease = prerelease.find(chunk)?.groupValues?.get(1) == "true",
                apkUrls = urls,
                versionName = tag.ifEmpty {
                    names.firstOrNull { !it.endsWith(".apk", ignoreCase = true) }?.trim().orEmpty()
                }.ifEmpty { null },
            )
        }
    }

    fun firstWithPackage(
        packageName: String,
        json: String,
        includePrereleases: Boolean = true,
        apkRegex: String? = null,
    ): GitHubReleaseRecord? = pick(json, includePrereleases, apkRegex) { rec ->
        ForgePackageEvidence.inText(packageName, rec.haystack)
    }

    fun firstApk(
        json: String,
        includePrereleases: Boolean = true,
        apkRegex: String? = null,
    ): GitHubReleaseRecord? = pick(json, includePrereleases, apkRegex) { rec ->
        rec.apkUrls.isNotEmpty()
    }

    private fun pick(
        json: String,
        includePrereleases: Boolean,
        apkRegex: String?,
        pred: (GitHubReleaseRecord) -> Boolean,
    ): GitHubReleaseRecord? {
        val pattern = GithubAppOptCodec.regexOrNull(apkRegex)
        return parse(json).firstOrNull { rec ->
            if (!includePrereleases && rec.prerelease) return@firstOrNull false
            if (!pred(rec)) return@firstOrNull false
            if (pattern != null && rec.apkUrls.none { pattern.containsMatchIn(GithubAppOptCodec.filename(it)) }) {
                return@firstOrNull false
            }
            true
        }?.let { rec ->
            if (pattern == null) rec
            else rec.copy(
                apkUrl = rec.apkUrls.first {
                    pattern.containsMatchIn(GithubAppOptCodec.filename(it))
                },
            )
        }
    }
}
