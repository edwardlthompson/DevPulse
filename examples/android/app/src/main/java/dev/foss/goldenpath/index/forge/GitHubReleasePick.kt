package dev.foss.goldenpath.index.forge

/** Bound repos (hint/paste) may ship APKs whose filenames omit the package id. */
object GitHubReleasePick {
    fun bound(
        packageName: String,
        json: String,
        includePrereleases: Boolean = true,
        apkRegex: String? = null,
    ): GitHubReleaseRecord? {
        val named = GitHubReleaseParser.firstWithPackage(
            packageName, json, includePrereleases, apkRegex,
        )
        if (!named?.apkUrl.isNullOrEmpty()) return named
        return GitHubReleaseParser.firstApk(json, includePrereleases, apkRegex)
    }
}
