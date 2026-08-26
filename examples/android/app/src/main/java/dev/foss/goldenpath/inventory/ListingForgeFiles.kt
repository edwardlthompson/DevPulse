package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.forge.FileDirectApkStore
import dev.foss.goldenpath.index.forge.FileGithubAppOptStore
import dev.foss.goldenpath.index.forge.GithubAppOpt
import java.io.File

object ListingForgeFiles {
    fun opt(filesDir: File, packageName: String): GithubAppOpt? =
        FileGithubAppOptStore(File(filesDir, "github_app_opts.tsv")).get(packageName)

    fun apk(filesDir: File, packageName: String): String? =
        FileDirectApkStore(File(filesDir, "direct_apks.tsv")).load()[packageName]
}
