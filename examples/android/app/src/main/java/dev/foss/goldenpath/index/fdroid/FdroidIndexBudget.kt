package dev.foss.goldenpath.index.fdroid

object FdroidIndexBudget {
    const val MAX_BYTES = 2_000_000
    const val HOST_WORKERS = 12
    const val HOST_EXTRA_MAX = 80

    fun hostResolve(repo: FdroidRepo): Boolean =
        repo.id == "official" || repo.id == "archive" ||
            repo.kind == FdroidRepoKind.Official || repo.kind == FdroidRepoKind.Archive

    fun extraHostResolve(repo: FdroidRepo): Boolean =
        repo.id == "izzy" || repo.kind == FdroidRepoKind.Izzy

    fun skipArchiveHost(officialOn: Boolean, repo: FdroidRepo): Boolean =
        officialOn && hostResolve(repo) && (repo.id == "archive" || repo.kind == FdroidRepoKind.Archive)

    fun extraHostAllowed(wanted: Int): Boolean =
        wanted in 1..HOST_EXTRA_MAX
}
