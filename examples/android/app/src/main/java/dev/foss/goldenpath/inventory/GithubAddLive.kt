package dev.foss.goldenpath.inventory

import android.content.Context
import dev.foss.goldenpath.index.forge.FileGithubVerifiedStore
import dev.foss.goldenpath.index.forge.FilePastedRepoStore
import dev.foss.goldenpath.index.forge.FileWatchedRepoStore
import dev.foss.goldenpath.index.forge.ForgeLibraryMatch
import dev.foss.goldenpath.index.forge.GithubAdd
import dev.foss.goldenpath.index.forge.GithubHintFiles
import dev.foss.goldenpath.index.forge.LibraryMatch
import java.io.File

object GithubAddLive {
    fun bind(context: Context, url: String, installed: List<InstalledApp>): GithubAddResult {
        val repo = GithubAdd.ownerRepo(url) ?: return GithubAddResult.Invalid
        val files = context.filesDir
        val pasted = FilePastedRepoStore(File(files, "pasted_repos.tsv"))
        val verified = FileGithubVerifiedStore(File(files, "github_verified.tsv"))
        val watched = FileWatchedRepoStore(File(files, "github_watched.tsv"))
        val matches = ForgeLibraryMatch.bind(repo, installed, GithubHintFiles.library(files))
        val bound = GithubAdd.persist(repo, matches, pasted, verified, watched)
        bound.forEach { match ->
            installed.find { it.packageName == match.packageName }?.let { AppReprobeLive.run(context, it) }
        }
        return when {
            bound.isNotEmpty() -> GithubAddResult.Bound(bound)
            matches.isNotEmpty() -> GithubAddResult.Pick(repo, matches)
            else -> GithubAddResult.Watched(repo)
        }
    }

    fun pick(context: Context, packageName: String, ownerRepo: String, installed: List<InstalledApp>): Boolean {
        val files = context.filesDir
        val ok = GithubAdd.persistPicked(
            packageName,
            ownerRepo,
            FilePastedRepoStore(File(files, "pasted_repos.tsv")),
            FileGithubVerifiedStore(File(files, "github_verified.tsv")),
            FileWatchedRepoStore(File(files, "github_watched.tsv")),
        )
        if (!ok) return false
        installed.find { it.packageName == packageName }?.let { AppReprobeLive.run(context, it) }
        return true
    }
}

sealed class GithubAddResult {
    data object Invalid : GithubAddResult()
    data class Bound(val matches: List<LibraryMatch>) : GithubAddResult()
    data class Pick(val ownerRepo: String, val matches: List<LibraryMatch>) : GithubAddResult()
    data class Watched(val ownerRepo: String) : GithubAddResult()
}
