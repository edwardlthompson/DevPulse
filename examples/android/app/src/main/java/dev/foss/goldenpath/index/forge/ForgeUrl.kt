package dev.foss.goldenpath.index.forge

object ForgeUrl {
    private val reserved = setOf(
        "about", "apps", "features", "login", "marketplace", "orgs", "settings",
        "signup", "sponsors", "topics",
    )

    fun downloadPage(raw: String?): String? {
        val url = raw?.trim().orEmpty()
        if (!url.startsWith("http://") && !url.startsWith("https://")) return null
        val hostPath = url.removePrefix("https://").removePrefix("http://").removePrefix("www.")
        val slash = hostPath.indexOf('/')
        if (slash <= 0) return null
        val host = hostPath.substring(0, slash).lowercase()
        val parts = hostPath.substring(slash + 1).split('/', '?', '#')
            .filter { it.isNotEmpty() }
        return when (host) {
            "github.com" -> github(parts)
            "gitlab.com" -> gitlab(parts)
            "codeberg.org" -> codeberg(parts)
            "bitbucket.org" -> bitbucket(parts)
            "sr.ht", "git.sr.ht" -> sourcehut(parts)
            else -> null
        }
    }

    private fun github(parts: List<String>): String? {
        val owner = parts.getOrNull(0) ?: return null
        val repo = parts.getOrNull(1)?.removeSuffix(".git") ?: return null
        if (owner in reserved || repo in reserved) return null
        return "https://github.com/$owner/$repo/releases"
    }

    private fun gitlab(parts: List<String>): String? {
        val owner = parts.getOrNull(0) ?: return null
        val repo = parts.getOrNull(1)?.removeSuffix(".git") ?: return null
        if (owner in reserved) return null
        return "https://gitlab.com/$owner/$repo/-/releases"
    }

    private fun codeberg(parts: List<String>): String? {
        val owner = parts.getOrNull(0) ?: return null
        val repo = parts.getOrNull(1)?.removeSuffix(".git") ?: return null
        return "https://codeberg.org/$owner/$repo/releases"
    }

    private fun bitbucket(parts: List<String>): String? {
        val owner = parts.getOrNull(0) ?: return null
        val repo = parts.getOrNull(1)?.removeSuffix(".git") ?: return null
        return "https://bitbucket.org/$owner/$repo/downloads"
    }

    private fun sourcehut(parts: List<String>): String? {
        val owner = parts.getOrNull(0) ?: return null
        val repo = parts.getOrNull(1)?.removeSuffix(".git") ?: return null
        if (!owner.startsWith("~")) return null
        return "https://git.sr.ht/$owner/$repo/refs"
    }
}
