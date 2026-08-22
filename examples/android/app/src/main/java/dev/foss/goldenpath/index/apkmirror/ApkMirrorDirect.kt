package dev.foss.goldenpath.index.apkmirror

import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifact
import dev.foss.goldenpath.inventory.UpdateArtifactMemory

object ApkMirrorDirect {
    fun resolve(
        packageName: String,
        pageUrl: String?,
        fetchPage: (String) -> String?,
    ): UpdateArtifact? {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return null
        UpdateArtifactMemory.forSource(pkg, RemoteReleasedSource.ApkMirror)?.let { return it }
        val start = pageUrl?.trim()?.takeIf { it.startsWith("https://") } ?: return null
        var html = fetchPage(start) ?: return null
        repeat(3) {
            ApkMirrorDownload.fileUrl(html)?.let { return remember(pkg, it) }
            val next = ApkMirrorDownload.nextPage(html) ?: return null
            html = fetchPage(next) ?: return null
        }
        return ApkMirrorDownload.fileUrl(html)?.let { remember(pkg, it) }
    }

    private fun remember(pkg: String, url: String): UpdateArtifact? {
        UpdateArtifactMemory.add(UpdateArtifact(pkg, RemoteReleasedSource.ApkMirror, url))
        return UpdateArtifactMemory.forSource(pkg, RemoteReleasedSource.ApkMirror)
    }
}
