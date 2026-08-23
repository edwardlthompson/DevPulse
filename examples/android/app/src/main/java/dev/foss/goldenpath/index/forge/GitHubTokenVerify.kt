package dev.foss.goldenpath.index.forge

enum class GitHubTokenOutcome {
    Accepted,
    Rejected,
    Unreachable,
    Cleared,
    Blank,
}

data class GitHubTokenCheck(
    val outcome: GitHubTokenOutcome,
    val hourlyLimit: Int? = null,
    val hourlyRemaining: Int? = null,
)

fun interface GitHubTokenClient {
    fun check(token: String): Result<GitHubSearchPage>
}

object GitHubTokenVerify {
    private val coreLimit = Regex(""""core"\s*:\s*\{[^}]*"limit"\s*:\s*(\d+)""")
    private val coreRemaining = Regex(""""core"\s*:\s*\{[^}]*"remaining"\s*:\s*(\d+)""")

    fun connect(draft: String, client: GitHubTokenClient, store: ForgeTokenStore): GitHubTokenCheck {
        val token = draft.trim()
        if (token.isEmpty()) return GitHubTokenCheck(GitHubTokenOutcome.Blank)
        val page = client.check(token).getOrElse { return GitHubTokenCheck(GitHubTokenOutcome.Unreachable) }
        val result = fromPage(page)
        if (result.outcome == GitHubTokenOutcome.Accepted) store.setToken(token)
        return result
    }

    fun disconnect(store: ForgeTokenStore): GitHubTokenCheck {
        store.setToken(null)
        return GitHubTokenCheck(GitHubTokenOutcome.Cleared)
    }

    fun fromPage(page: GitHubSearchPage): GitHubTokenCheck {
        if (page.statusCode == 401 || badCredentials(page)) {
            return GitHubTokenCheck(GitHubTokenOutcome.Rejected)
        }
        if (page.statusCode !in 200..299) return GitHubTokenCheck(GitHubTokenOutcome.Unreachable)
        return GitHubTokenCheck(
            GitHubTokenOutcome.Accepted,
            coreLimit.find(page.body)?.groupValues?.get(1)?.toIntOrNull(),
            coreRemaining.find(page.body)?.groupValues?.get(1)?.toIntOrNull(),
        )
    }

    private fun badCredentials(page: GitHubSearchPage): Boolean =
        page.statusCode == 403 && page.body.contains("Bad credentials", ignoreCase = true)
}
