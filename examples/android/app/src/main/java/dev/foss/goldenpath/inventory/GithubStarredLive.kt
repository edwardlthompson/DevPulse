package dev.foss.goldenpath.inventory

import android.content.Context
import android.util.Log
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.index.forge.FileGithubVerifiedStore
import dev.foss.goldenpath.index.forge.FilePastedRepoStore
import dev.foss.goldenpath.index.forge.FileWatchedRepoStore
import dev.foss.goldenpath.index.forge.ForgeBackoff
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import dev.foss.goldenpath.index.forge.GitHubStarredHttp
import dev.foss.goldenpath.index.forge.GitHubStarredScan
import dev.foss.goldenpath.index.forge.GithubAdd
import dev.foss.goldenpath.index.forge.GithubHintFiles
import dev.foss.goldenpath.index.forge.StarredBindResult
import java.io.File

object GithubStarredLive {
    fun run(context: Context, installed: List<InstalledApp>): StarredBindResult {
        val token = EncryptedForgeTokenStore.wrap(context).getToken()?.trim().orEmpty()
        if (token.isEmpty()) return StarredBindResult(0, 0, 401)
        val http = GitHubStarredHttp(token)
        val pages = mutableListOf<GitHubSearchPage>()
        for (n in 1..GitHubStarredScan.MAX_PAGES) {
            val page = runCatching { http.page(n) }.getOrElse { return StarredBindResult(0, 0, 408) }
            if (page.statusCode == 403 || page.statusCode == 429) {
                ForgeBackoff.nextDelayMs(page.statusCode, 1, page.retryAfterSec)
                return StarredBindResult(0, 0, page.statusCode)
            }
            if (page.statusCode !in 200..299) return StarredBindResult(0, 0, page.statusCode)
            pages += page
            if (page.body.isBlank() || page.body.trim() == "[]") break
        }
        val library = GithubHintFiles.library(context.filesDir)
        val (matches, result) = GitHubStarredScan.bind(pages, installed, library)
        val files = context.filesDir
        val pasted = FilePastedRepoStore(File(files, "pasted_repos.tsv"))
        val verified = FileGithubVerifiedStore(File(files, "github_verified.tsv"))
        val watched = FileWatchedRepoStore(File(files, "github_watched.tsv"))
        matches.groupBy { it.ownerRepo }.forEach { (repo, group) ->
            GithubAdd.persist(repo, group, pasted, verified, watched)
        }
        matches.forEach { match ->
            installed.find { it.packageName == match.packageName }?.let { AppReprobeLive.run(context, it) }
        }
        Log.i("DevPulse", "starred matched=${result.matched} stars=${result.stars}")
        return result
    }
}
