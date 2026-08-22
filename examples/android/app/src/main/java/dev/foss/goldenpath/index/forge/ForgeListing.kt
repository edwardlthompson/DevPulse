package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifact
import dev.foss.goldenpath.inventory.UpdateArtifactMemory

object ForgeListing {
    private val repo = Regex(
        """https?://(?:www\.)?github\.com/([^/]+)/([^/#?]+)""",
        RegexOption.IGNORE_CASE,
    )

    fun ownerRepo(pageUrl: String?): String? {
        val match = repo.find(pageUrl.orEmpty()) ?: return null
        val owner = match.groupValues[1]
        val name = match.groupValues[2].removeSuffix(".git")
        if (owner.equals("orgs", ignoreCase = true) || name.equals("releases", ignoreCase = true)) return null
        if (owner.isEmpty() || name.isEmpty()) return null
        return "$owner/$name"
    }

    fun fromReleases(packageName: String, json: String): UpdateArtifact? {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return null
        val url = GitHubReleaseParser.firstWithPackage(pkg, json)?.apkUrl ?: return null
        GitHubNotes.rememberApk(pkg, url)
        return UpdateArtifactMemory.forSource(pkg, RemoteReleasedSource.Forge)
    }
}
