package dev.foss.goldenpath.index.forge

object ObtainiumImport {
    data class Result(val imported: Int, val skipped: Int, val rows: List<Pair<String, String>>)

    fun parse(json: String): Result {
        val inner = appsInner(json) ?: return Result(0, 0, emptyList())
        val rows = mutableListOf<Pair<String, String>>()
        var skipped = 0
        inner.split(Regex("\\}\\s*,\\s*\\{")).forEach { chunk ->
            val id = field(chunk, "id")
            val url = field(chunk, "url")
            if (id.isEmpty() || '.' !in id || GithubAdd.ownerRepo(url) == null) {
                skipped += 1
                return@forEach
            }
            rows += id to url
        }
        return Result(rows.size, skipped, rows)
    }

    fun persist(
        rows: List<Pair<String, String>>,
        pasted: PastedRepoStore,
        verified: GithubVerifiedStore,
        watched: WatchedRepoStore,
    ): Int {
        var saved = 0
        rows.forEach { (id, url) ->
            val repo = GithubAdd.ownerRepo(url) ?: return@forEach
            if (GithubAdd.persistPicked(id, repo, pasted, verified, watched)) saved += 1
        }
        return saved
    }

    private fun appsInner(json: String): String? {
        val appsAt = json.indexOf("\"apps\"")
        if (appsAt < 0) return null
        val start = json.indexOf('[', appsAt)
        val end = json.indexOf(']', start)
        if (start < 0 || end < 0) return null
        return json.substring(start + 1, end).trim().trimStart('[').trimEnd(']').trim()
            .takeIf { it.isNotEmpty() && it != "[]" }
    }

    private fun field(chunk: String, name: String): String =
        Regex(""""$name"\s*:\s*"([^"]+)"""").find(chunk)?.groupValues?.get(1)?.trim().orEmpty()
}
