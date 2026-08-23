package dev.foss.goldenpath.index.fdroid

import dev.foss.goldenpath.inventory.ListingChannels
import dev.foss.goldenpath.inventory.ListingExtra
import dev.foss.goldenpath.inventory.ListingExtraBook
import dev.foss.goldenpath.inventory.UpdateArtifact
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import dev.foss.goldenpath.inventory.UpdateNotes
import dev.foss.goldenpath.inventory.UpdateNotesMemory

object FdroidNotes {
    fun remember(records: List<FdroidAppRecord>, wanted: Set<String>) {
        records.asSequence().filter { it.packageName in wanted }.forEach { rec ->
            val source = ListingChannels.sourceForRepo(rec.repoId)
            rec.whatsNew?.let { UpdateNotesMemory.putIfAbsent(rec.packageName, UpdateNotes(it, source)) }
            ListingExtraBook.put(
                rec.packageName,
                source,
                ListingExtra(rec.apkSizeBytes, rec.antiFeatures, rec.minSdk, rec.nativeCodes),
            )
            val url = FdroidApkUrl.of(rec.repoId, rec.apkName) ?: return@forEach
            UpdateArtifactMemory.add(
                UpdateArtifact(
                    rec.packageName,
                    source,
                    url,
                    rec.suggestedVersionName,
                    sha256 = rec.apkSha256,
                    nativeCodes = rec.nativeCodes,
                ),
            )
        }
    }
}
