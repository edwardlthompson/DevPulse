package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.index.fdroid.FdroidAppRecord

data class LeftoverHint(
    val host: ForgeHost,
    val ownerRepo: String,
    val ms: Long? = null,
    val versionName: String? = null,
)

object FdroidLeftoverHints {
    fun hints(records: List<FdroidAppRecord>, wanted: Set<String>): Map<String, LeftoverHint> {
        val best = linkedMapOf<String, Pair<Int, LeftoverHint>>()
        for (rec in records) {
            if (rec.packageName !in wanted) continue
            putBest(best, rec.packageName, rec.repoId, rec.sourceCode, rec.lastUpdatedMs, rec.suggestedVersionName)
        }
        return best.mapValues { it.value.second }
    }

    fun merge(
        records: List<FdroidAppRecord>,
        wanted: Set<String>,
        pasted: Map<String, String> = emptyMap(),
    ): Map<String, LeftoverHint> = pastedHints(pasted) + hints(records, wanted)

    fun leftover(raw: String?): LeftoverHint? {
        val page = ForgeUrl.downloadPage(raw) ?: return null
        return when {
            page.startsWith("https://gitlab.com/") ->
                ownerRepo(page, "https://gitlab.com/", "/-/releases")?.let { LeftoverHint(ForgeHost.GitLab, it) }
            page.startsWith("https://codeberg.org/") ->
                ownerRepo(page, "https://codeberg.org/", "/releases")?.let { LeftoverHint(ForgeHost.Codeberg, it) }
            else -> null
        }
    }

    private fun pastedHints(pasted: Map<String, String>): Map<String, LeftoverHint> =
        pasted.mapNotNull { (pkg, url) -> leftover(url)?.let { pkg to it } }.toMap()

    private fun ownerRepo(page: String, prefix: String, suffix: String): String? {
        if (!page.startsWith(prefix) || !page.endsWith(suffix)) return null
        val ownerRepo = page.removePrefix(prefix).removeSuffix(suffix)
        val slash = ownerRepo.indexOf('/')
        if (slash <= 0 || slash == ownerRepo.lastIndex) return null
        return ownerRepo
    }

    private fun putBest(
        best: MutableMap<String, Pair<Int, LeftoverHint>>,
        packageName: String,
        repoId: String,
        sourceCode: String?,
        ms: Long?,
        versionName: String?,
    ) {
        val base = leftover(sourceCode) ?: return
        val rank = FdroidGithubHints.rankOf(repoId)
        val prev = best[packageName]
        if (prev == null || rank < prev.first) {
            best[packageName] = rank to base.copy(ms = ms, versionName = versionName)
        }
    }
}
