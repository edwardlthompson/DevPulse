package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import dev.foss.goldenpath.index.fdroid.FdroidIndexBytes

data class GithubHint(
    val ownerRepo: String,
    val ms: Long? = null,
    val versionName: String? = null,
)

object FdroidGithubHints {
    private val sourceCode = Regex(""""sourceCode"\s*:\s*"([^"]+)"""")
    private val lastUpdated = Regex(""""lastUpdated"\s*:\s*(\d+)""")
    private val suggestedName = Regex(""""suggestedVersionName"\s*:\s*"([^"]+)"""")

    fun hints(records: List<FdroidAppRecord>, wanted: Set<String>): Map<String, GithubHint> {
        val best = linkedMapOf<String, Pair<Int, GithubHint>>()
        for (rec in records) {
            if (rec.packageName !in wanted) continue
            putBest(best, rec.packageName, rec.repoId, rec.sourceCode, rec.lastUpdatedMs, rec.suggestedVersionName)
        }
        return best.mapValues { it.value.second }
    }

    fun harvest(raw: ByteArray, repoId: String): Map<String, GithubHint> {
        val best = linkedMapOf<String, Pair<Int, GithubHint>>()
        var from = 0
        while (true) {
            val at = FdroidIndexBytes.indexOf(raw, "\"packageName\"", from)
            if (at < 0) break
            from = at + 13
            val name = FdroidIndexBytes.readJsonString(raw, from) ?: continue
            if (name.isEmpty() || '.' !in name) continue
            val chunk = FdroidIndexBytes.objectSlice(raw, at)
            putBest(
                best,
                name,
                repoId,
                sourceCode.find(chunk)?.groupValues?.get(1),
                lastUpdated.find(chunk)?.groupValues?.get(1)?.toLongOrNull(),
                suggestedName.find(chunk)?.groupValues?.get(1)?.trim()?.ifEmpty { null },
            )
        }
        return best.mapValues { it.value.second }
    }

    fun mergeLibrary(
        persisted: Map<String, String>,
        harvested: List<Pair<String, Map<String, GithubHint>>>,
    ): Map<String, GithubHint> {
        val best = linkedMapOf<String, Pair<Int, GithubHint>>()
        persisted.forEach { (pkg, ownerRepo) ->
            putBest(best, pkg, "other", "https://github.com/$ownerRepo", null, null)
        }
        harvested.forEach { (repoId, hints) ->
            hints.forEach { (pkg, hint) ->
                putBest(best, pkg, repoId, "https://github.com/${hint.ownerRepo}", hint.ms, hint.versionName)
            }
        }
        return best.mapValues { it.value.second }
    }

    fun map(records: List<FdroidAppRecord>, wanted: Set<String>): Map<String, String> =
        hints(records, wanted).mapValues { it.value.ownerRepo }

    fun rankOf(repoId: String): Int = when (repoId) {
        "official" -> 0
        "izzy" -> 1
        "archive" -> 2
        "guardian" -> 3
        "calyx" -> 4
        else -> 5
    }

    fun ownerRepo(raw: String?): String? {
        val page = ForgeUrl.downloadPage(raw) ?: return null
        val prefix = "https://github.com/"
        val suffix = "/releases"
        if (!page.startsWith(prefix) || !page.endsWith(suffix)) return null
        val ownerRepo = page.removePrefix(prefix).removeSuffix(suffix)
        val slash = ownerRepo.indexOf('/')
        if (slash <= 0 || slash == ownerRepo.lastIndex) return null
        if (ownerRepo.indexOf('/', slash + 1) >= 0) return null
        return ownerRepo
    }

    private fun putBest(
        best: MutableMap<String, Pair<Int, GithubHint>>,
        packageName: String,
        repoId: String,
        sourceCode: String?,
        ms: Long?,
        versionName: String?,
    ) {
        val ownerRepo = ownerRepo(sourceCode) ?: return
        val rank = rankOf(repoId)
        val prev = best[packageName]
        if (prev == null || rank < prev.first) {
            best[packageName] = rank to GithubHint(ownerRepo, ms, versionName)
        }
    }
}
