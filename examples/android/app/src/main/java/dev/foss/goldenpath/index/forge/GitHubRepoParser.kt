package dev.foss.goldenpath.index.forge

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object GitHubRepoParser {
    private val fullName = Regex(""""full_name"\s*:\s*"([^"]+)"""")
    private val name = Regex(""""name"\s*:\s*"([^"]+)"""")
    private val pushed = Regex(""""pushed_at"\s*:\s*"([^"]+)"""")
    private val description = Regex(""""description"\s*:\s*"([^"]*)"""")

    fun parse(json: String): List<ForgeCandidate> {
        val items = json.substringAfter("\"items\"", "")
        if (items.isEmpty()) return emptyList()
        return fullName.findAll(items).map { match ->
            val repo = match.groupValues[1]
            val window = items.substring(match.range.first).take(4_000)
            ForgeCandidate(
                host = ForgeHost.GitHub,
                ownerRepo = repo,
                packageId = null,
                title = name.find(window)?.groupValues?.get(1) ?: repo.substringAfter('/'),
                latestCommitMs = isoMs(pushed.find(window)?.groupValues?.get(1)),
                latestReleaseMs = null,
                archived = window.contains("\"archived\":true") || window.contains("\"archived\": true"),
                description = description.find(window)?.groupValues?.get(1)?.ifEmpty { null },
            )
        }.toList()
    }

    internal fun isoMs(raw: String?): Long? {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return null
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        return runCatching { parser.parse(text)?.time }.getOrNull()
    }
}
