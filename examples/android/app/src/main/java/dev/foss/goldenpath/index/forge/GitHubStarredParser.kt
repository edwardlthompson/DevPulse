package dev.foss.goldenpath.index.forge

object GitHubStarredParser {
    private val fullName = Regex(""""full_name"\s*:\s*"([^"]+)"""")

    fun ownerRepos(json: String): List<String> {
        if (json.isBlank() || json == "[]" || json == "{}") return emptyList()
        return fullName.findAll(json).map { it.groupValues[1].trim() }
            .filter { it.contains('/') && it.count { ch -> ch == '/' } == 1 }
            .distinct()
            .toList()
    }
}
