package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.R

data class GitHubTokenStep(
    val textRes: Int,
    val url: String? = null,
)

object GitHubTokenGuide {
    const val LOGIN_URL = "https://github.com/login"
    const val FINE_LIST_URL = "https://github.com/settings/personal-access-tokens"
    const val CREATE_URL = "https://github.com/settings/personal-access-tokens/new"
    const val CLASSIC_URL = "https://github.com/settings/tokens/new"

    val steps = listOf(
        GitHubTokenStep(R.string.forge_token_step_1, LOGIN_URL),
        GitHubTokenStep(R.string.forge_token_step_2, FINE_LIST_URL),
        GitHubTokenStep(R.string.forge_token_step_3, CREATE_URL),
        GitHubTokenStep(R.string.forge_token_step_4),
        GitHubTokenStep(R.string.forge_token_step_5),
    )

    fun isGithubHttps(url: String): Boolean = url.startsWith("https://github.com/")
}
