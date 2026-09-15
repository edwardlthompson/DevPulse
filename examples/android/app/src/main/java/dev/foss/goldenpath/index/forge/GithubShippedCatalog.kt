package dev.foss.goldenpath.index.forge

import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.InputStreamReader

object GithubShippedCatalog {
    const val ASSET = "github-repos/verified.tsv"

    fun parse(raw: String): Map<String, String> = GithubVerifiedCodec.decode(raw)

    fun fromAssets(assets: AssetManager): Map<String, String> = parse(
        runCatching {
            assets.open(ASSET).use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
            }
        }.getOrDefault(""),
    )
}
