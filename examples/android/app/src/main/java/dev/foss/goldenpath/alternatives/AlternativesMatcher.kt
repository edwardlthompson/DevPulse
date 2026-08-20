package dev.foss.goldenpath.alternatives

data class AlternativeHit(
    val packageName: String,
    val title: String,
    val score: Int,
    val sourceUrl: String,
)

object AlternativesMatcher {
    fun match(queryTitle: String, candidates: List<AlternativeHit>, minScore: Int = 1): List<AlternativeHit> {
        val needle = queryTitle.lowercase()
        return candidates
            .map { hit ->
                val score = overlap(needle, hit.title.lowercase()) + hit.score
                hit.copy(score = score)
            }
            .filter { it.score >= minScore }
            .sortedByDescending { it.score }
    }

    private fun overlap(left: String, right: String): Int =
        left.split(" ").count { token -> token.isNotBlank() && right.contains(token) }
}

object SourcesList {
    fun merge(urls: List<String>): List<String> =
        urls.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
}
