package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifact
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import dev.foss.goldenpath.inventory.UpdateNotes
import dev.foss.goldenpath.inventory.UpdateNotesMemory

object GitHubNotes {
    fun remember(packageName: String, text: String?) {
        val notes = text?.trim()?.takeIf { it.isNotEmpty() } ?: return
        UpdateNotesMemory.put(packageName, UpdateNotes(notes, RemoteReleasedSource.Forge))
    }

    fun rememberApk(packageName: String, apkUrl: String?, versionName: String? = null) {
        val url = apkUrl ?: return
        UpdateArtifactMemory.add(UpdateArtifact(packageName, RemoteReleasedSource.Forge, url, versionName))
    }
}
