package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import dev.foss.goldenpath.index.forge.FdroidGithubHints
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.forge.PackageIdAliases
import dev.foss.goldenpath.index.forge.PastedRepoCodec

object ReleaseRefreshHints {
    fun github(
        records: List<FdroidAppRecord>,
        wanted: Set<String>,
        verified: Map<String, String> = emptyMap(),
        pasted: Map<String, String> = emptyMap(),
    ): Map<String, GithubHint> {
        val aliasWanted = wanted.flatMap { PackageIdAliases.keys(it) }.toSet()
        val merged = verified.mapValues { GithubHint(it.value) } +
            FdroidGithubHints.hints(records, aliasWanted) + PastedRepoCodec.hints(pasted)
        return PackageIdAliases.expand(wanted, merged)
    }
}
