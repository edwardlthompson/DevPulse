package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.play.PlaySourceHints
import java.io.File

object CacheWipe {
    fun remotes(filesDir: File) {
        RemoteReleaseMemory.clear()
        ListingExtraBook.clear()
        UpdateArtifactMemory.clear()
        DumpChunkBook.clear()
        PlaySourceHints.clear()
        RefreshResume.clear()
        RefreshSuccessBook.clear()
        RefreshFailBook.clear()
        File(filesDir, "remote_releases.json").delete()
        File(filesDir, "fdroid-index").deleteRecursively()
        filesDir.listFiles()?.filter { it.name.startsWith("dump_chunk_") }?.forEach { it.delete() }
    }
}
