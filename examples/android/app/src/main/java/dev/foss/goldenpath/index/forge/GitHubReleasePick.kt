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
        return GitHubReleaseParser.firstApk(json, includePrereleases, apkRegex, packageName)
    }

    fun leftover(
        packageName: String,
        label: String,
        ownerRepo: String,
        json: String,
    ): GitHubReleaseRecord? {
        val named = GitHubReleaseParser.firstWithPackage(packageName, json)
        if (!named?.apkUrl.isNullOrEmpty()) return named
        val slug = ForgeSlug.matches(ownerRepo, label, packageName)
        val last = packageName.substringAfterLast('.')
        val tail = last.length >= 4 && ownerRepo.contains(last, ignoreCase = true)
        val apk = if (slug || tail) {
            GitHubReleaseParser.firstApk(json, packageName = packageName)
        } else {
            null
        }
        return apk ?: named
    }
}
