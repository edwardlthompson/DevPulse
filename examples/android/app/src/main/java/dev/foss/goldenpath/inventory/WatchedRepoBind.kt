package dev.foss.goldenpath.inventory

import android.content.Context
import dev.foss.goldenpath.index.forge.FileGithubVerifiedStore
import dev.foss.goldenpath.index.forge.FilePastedRepoStore
import dev.foss.goldenpath.index.forge.FileWatchedRepoStore
import dev.foss.goldenpath.index.forge.ForgeLibraryMatch
import dev.foss.goldenpath.index.forge.GithubAdd
import dev.foss.goldenpath.index.forge.GithubHintFiles
import java.io.File

object WatchedRepoBind {
    fun onInstalled(context: Context, packageName: String) {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || '.' !in pkg) return
        val files = context.filesDir
        val watched = FileWatchedRepoStore(File(files, "github_watched.tsv"))
        val repos = watched.load()
        if (repos.isEmpty()) return
        val installed = listOf(stub(pkg))
        val library = GithubHintFiles.library(files)
        val pasted = FilePastedRepoStore(File(files, "pasted_repos.tsv"))
        val verified = FileGithubVerifiedStore(File(files, "github_verified.tsv"))
        repos.forEach { repo ->
            val matches = ForgeLibraryMatch.bind(repo, installed, library)
            GithubAdd.persist(repo, matches, pasted, verified, watched)
        }
    }

    private fun stub(pkg: String) = InstalledApp(
        packageName = pkg,
        label = pkg,
        versionName = null,
        versionCode = 0L,
        lastUpdateTimeMs = 0L,
        firstInstallTimeMs = 0L,
        minSdk = 1,
        targetSdk = 1,
        isSystemApp = false,
    )
}
