package dev.foss.goldenpath.index.play

import dev.foss.goldenpath.index.aurora.AuroraPlayDetails
import dev.foss.goldenpath.index.aurora.AuroraPlayScan
import dev.foss.goldenpath.index.aurora.AuroraPlayStatus
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateUrls

object PlayScan {
    fun toOffer(
        packageName: String,
        client: PlayPageClient?,
        aurora: AuroraPlayDetails? = null,
    ): RemoteReleaseOffer {
        if (aurora != null) {
            val app = runCatching { aurora.getMany(listOf(packageName))[packageName] }.getOrNull()
            if (app != null && app.status != AuroraPlayStatus.Unknown) {
                RefreshTrace.line("play $packageName aurora ${app.status.name.lowercase()}")
                return AuroraPlayScan.toOffer(packageName, app)
            }
            RefreshTrace.line("play $packageName aurora unknown")
        }
        if (client == null) return unknown()
        val page = runCatching { client.get(packageName) }.getOrElse {
            RefreshTrace.line("play $packageName fail ${it.javaClass.simpleName}: ${it.message}")
            return unknown()
        }
        val lookup = PlayHtmlParser.parse(page.body)
        val presence = PlayListing.of(page.code, page.body, lookup)
        RefreshTrace.line("play $packageName http ${page.code} ${presence.name.lowercase()} ${page.body.length}B")
        return when (presence) {
            PlayPresence.Listed -> RemoteReleaseOffer(
                source = RemoteReleasedSource.Play,
                ms = lookup.updatedOnMs,
                versionName = lookup.publishedVersion,
                pageUrl = UpdateUrls.play(packageName),
                listed = true,
            )
            PlayPresence.Missing -> recovered(packageName) ?: RemoteReleaseOffer(
                source = RemoteReleasedSource.Play,
                listed = false,
            )
            PlayPresence.Unknown -> unknown()
        }
    }

    private fun recovered(packageName: String): RemoteReleaseOffer? {
        val lookup = WaybackPlay.client?.recover(packageName) ?: return null
        val ms = lookup.updatedOnMs ?: return null
        RefreshTrace.line("play $packageName wayback recovered")
        return RemoteReleaseOffer(
            source = RemoteReleasedSource.Play,
            ms = ms,
            versionName = lookup.publishedVersion,
            pageUrl = UpdateUrls.play(packageName),
            listed = false,
        )
    }

    private fun unknown(): RemoteReleaseOffer =
        RemoteReleaseOffer(source = RemoteReleasedSource.Play, listed = false, known = false)
}
