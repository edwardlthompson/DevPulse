package dev.foss.goldenpath.index.forge

object GithubAdd {
    fun ownerRepo(url: String): String? {
        val href = url.trim()
        if (href.isEmpty()) return null
        return FdroidGithubHints.ownerRepo(href)
    }

    fun persist(
        ownerRepo: String,
        matches: List<LibraryMatch>,
        pasted: PastedRepoStore,
        verified: GithubVerifiedStore,
        watched: WatchedRepoStore,
    ): List<LibraryMatch> {
        val repo = WatchedRepoCodec.normalize(ownerRepo) ?: return emptyList()
        val auto = ForgeLibraryMatch.autoBind(matches)
        if (auto.isEmpty()) {
            watched.add(repo)
            return emptyList()
        }
        auto.forEach { match ->
            pasted.put(match.packageName, "https://github.com/${match.ownerRepo}")
            verified.put(match.packageName, match.ownerRepo)
        }
        watched.remove(repo)
        return auto
    }

    fun persistPicked(
        packageName: String,
        ownerRepo: String,
        pasted: PastedRepoStore,
        verified: GithubVerifiedStore,
        watched: WatchedRepoStore,
    ): Boolean {
        val repo = WatchedRepoCodec.normalize(ownerRepo) ?: return false
        val clean = PastedRepoCodec.normalize(packageName, "https://github.com/$repo") ?: return false
        pasted.put(clean.first, clean.second)
        verified.put(clean.first, repo)
        watched.remove(repo)
        return true
    }
}
