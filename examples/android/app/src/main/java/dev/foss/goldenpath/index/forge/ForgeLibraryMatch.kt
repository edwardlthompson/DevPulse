package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.InstalledApp

enum class LibraryMatchRank {
    ExactPackage,
    SuffixVariant,
    ReleaseEvidence,
    Unmatched,
}

data class LibraryMatch(
    val packageName: String,
    val ownerRepo: String,
    val rank: LibraryMatchRank,
)

object ForgeLibraryMatch {
    fun bind(
        ownerRepo: String,
        installed: List<InstalledApp>,
        library: Map<String, String>,
        releaseHaystack: String? = null,
    ): List<LibraryMatch> {
        val repo = ownerRepo.trim()
        if (!repo.contains('/')) return emptyList()
        val hints = library.mapValues { GithubHint(it.value) }
        val exact = installed.mapNotNull { app ->
            val mapped = library[app.packageName]
            if (mapped != null && mapped.equals(repo, ignoreCase = true)) {
                LibraryMatch(app.packageName, repo, LibraryMatchRank.ExactPackage)
            } else {
                null
            }
        }
        if (exact.isNotEmpty()) return exact
        val suffix = installed.mapNotNull { app ->
            if (library[app.packageName] != null) return@mapNotNull null
            val aliased = PackageIdAliases.hint(app.packageName, hints) ?: return@mapNotNull null
            if (!aliased.ownerRepo.equals(repo, ignoreCase = true)) return@mapNotNull null
            LibraryMatch(app.packageName, repo, LibraryMatchRank.SuffixVariant)
        }
        if (suffix.isNotEmpty()) return suffix
        val hay = releaseHaystack.orEmpty()
        if (hay.isNotEmpty()) {
            val evidence = installed.mapNotNull { app ->
                if (!ForgePackageEvidence.inText(app.packageName, hay)) return@mapNotNull null
                LibraryMatch(app.packageName, repo, LibraryMatchRank.ReleaseEvidence)
            }
            if (evidence.isNotEmpty()) return evidence
        }
        return emptyList()
    }

    fun autoBind(matches: List<LibraryMatch>): List<LibraryMatch> =
        matches.filter {
            it.rank == LibraryMatchRank.ExactPackage || it.rank == LibraryMatchRank.SuffixVariant
        }
}
