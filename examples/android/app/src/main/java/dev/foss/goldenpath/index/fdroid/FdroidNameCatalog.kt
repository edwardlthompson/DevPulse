package dev.foss.goldenpath.index.fdroid

import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.InputStreamReader

class FdroidNameCatalog(private val names: Map<String, Set<String>>) {
    fun loaded(repoId: String): Boolean = !names[alias(repoId)].isNullOrEmpty()

    fun size(repoId: String): Int = names[alias(repoId)]?.size ?: 0

    fun probe(repoId: String, wanted: Set<String>): Set<String> {
        val set = names[alias(repoId)]
        if (set.isNullOrEmpty()) return wanted
        return wanted.filterTo(linkedSetOf()) { it in set }
    }

    companion object {
        fun parse(official: String, izzy: String): FdroidNameCatalog {
            val off = lines(official)
            return FdroidNameCatalog(
                mapOf(
                    "official" to off,
                    "archive" to off,
                    "izzy" to lines(izzy),
                ),
            )
        }

        fun fromAssets(assets: AssetManager): FdroidNameCatalog = parse(
            readAsset(assets, "fdroid-names/official.txt"),
            readAsset(assets, "fdroid-names/izzy.txt"),
        )

        private fun alias(repoId: String): String = repoId.trim().lowercase()

        private fun lines(raw: String): Set<String> =
            raw.lineSequence().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.toSet()

        private fun readAsset(assets: AssetManager, path: String): String = runCatching {
            assets.open(path).use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
            }
        }.getOrDefault("")
    }
}
