package dev.foss.goldenpath.inventory

import android.content.Context
import dev.foss.goldenpath.network.NetworkUnmetered
import java.io.File
import kotlinx.coroutines.flow.first

object UpdatePrefetchLive {
    suspend fun run(context: Context, wanted: Collection<String> = emptySet()): Int {
        val prefs = InventoryPreferences(context)
        if (!prefs.updatePrefetchEnabled.first()) return 0
        UpdateArtifactStore.hydrate(context.filesDir)
        val artifacts = UpdatePrefetch.scoped(
            UpdateArtifactMemory.byPackage.values.flatten(),
            wanted,
        )
        return UpdatePrefetch.run(
            enabled = true,
            unmetered = NetworkUnmetered.isUnmetered(context),
            cacheDir = File(context.cacheDir, "updates"),
            artifacts = artifacts,
            fetchFile = { url, dest -> ApkHttpFetcher.toFile(url, dest, null) },
            inspect = { file -> ApkArchiveIdentity.inspect(context.packageManager, file) },
            installed = { pkg -> ApkArchiveIdentity.installed(context.packageManager, pkg) },
            auroraPlay = prefs.auroraPlayEnabled.first(),
        )
    }
}
