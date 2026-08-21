package dev.foss.goldenpath.index.fdroid

object FdroidIndexBudget {
    fun hostResolve(repo: FdroidRepo): Boolean =
        repo.id == "official" || repo.id == "archive" ||
            repo.kind == FdroidRepoKind.Official || repo.kind == FdroidRepoKind.Archive

    fun extraHostResolve(repo: FdroidRepo): Boolean =
        repo.id == "izzy" || repo.kind == FdroidRepoKind.Izzy
}
