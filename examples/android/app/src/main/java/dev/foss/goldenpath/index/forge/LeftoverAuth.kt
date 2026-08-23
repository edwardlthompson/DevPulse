package dev.foss.goldenpath.index.forge

object LeftoverAuth {
    fun header(host: ForgeHost, token: String?): Pair<String, String>? {
        val secret = token?.trim()?.ifEmpty { null } ?: return null
        return when (host) {
            ForgeHost.GitLab -> "PRIVATE-TOKEN" to secret
            ForgeHost.Codeberg -> "Authorization" to "token $secret"
            ForgeHost.GitHub -> null
        }
    }

    fun key(host: ForgeHost): String? = when (host) {
        ForgeHost.GitLab -> "gitlab_token"
        ForgeHost.Codeberg -> "codeberg_token"
        ForgeHost.GitHub -> null
    }
}
