package dev.foss.goldenpath.index.aurora

import dev.foss.goldenpath.inventory.ProbeCache
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateUrls

object AuroraPlayScan {
    fun toOffer(packageName: String, app: AuroraPlayApp): RemoteReleaseOffer = when (app.status) {
        AuroraPlayStatus.Listed -> RemoteReleaseOffer(
            source = RemoteReleasedSource.Play,
            ms = app.updatedOnMs,
            versionName = app.versionName,
            pageUrl = UpdateUrls.play(packageName),
            listed = true,
        )
        AuroraPlayStatus.Missing -> RemoteReleaseOffer(
            source = RemoteReleasedSource.Play,
            listed = false,
            known = true,
        )
        AuroraPlayStatus.Unknown -> RemoteReleaseOffer(
            source = RemoteReleasedSource.Play,
            listed = false,
            known = false,
        )
    }

    fun applyBatch(
        packageNames: List<String>,
        details: AuroraPlayDetails,
        nowMs: Long,
    ): Map<String, RemoteReleaseOffer> {
        val wanted = packageNames.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val first = linkedMapOf<String, AuroraPlayApp>()
        wanted.chunked(AuroraPlayLookup.CHUNK).forEach { chunk ->
            val fetched = runCatching { details.getMany(chunk) }.getOrElse {
                RefreshTrace.line("aurora batch ${chunk.size} fail ${it.javaClass.simpleName}: ${it.message}")
                return@forEach
            }
            RefreshTrace.line("aurora batch ${chunk.size} listed=${fetched.values.count { it.status == AuroraPlayStatus.Listed }}")
            chunk.forEach { pkg -> first[pkg] = fetched[pkg] ?: AuroraPlayApp(AuroraPlayStatus.Missing) }
        }
        val misses = first.filter { it.value.status == AuroraPlayStatus.Missing }.keys.toList()
        if (misses.isNotEmpty()) {
            RefreshTrace.line("aurora second-pass ${misses.size} misses")
            misses.chunked(AuroraPlayLookup.CHUNK).forEach { chunk ->
                val extra = runCatching { details.getMany(chunk) }.getOrDefault(emptyMap())
                extra.forEach { (pkg, app) -> if (app.status == AuroraPlayStatus.Listed) first[pkg] = app }
            }
        }
        return first.mapNotNull { (pkg, app) ->
            if (app.status == AuroraPlayStatus.Unknown) null
            else pkg to ProbeCache.stamp(toOffer(pkg, app), nowMs)
        }.toMap()
    }
}
