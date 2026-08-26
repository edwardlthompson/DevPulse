package dev.foss.goldenpath.index.forge

import java.io.File

object GithubHintFiles {
    fun load(filesDir: File): Map<String, GithubHint> {
        val verified = FileGithubVerifiedStore(File(filesDir, "github_verified.tsv")).load()
        val pasted = FilePastedRepoStore(File(filesDir, "pasted_repos.tsv")).load()
        return verified.mapValues { GithubHint(it.value) } + PastedRepoCodec.hints(pasted)
    }

    fun library(filesDir: File): Map<String, String> =
        load(filesDir).mapValues { it.value.ownerRepo }

    fun hint(filesDir: File, packageName: String): GithubHint? =
        PackageIdAliases.hint(packageName, load(filesDir))
}
