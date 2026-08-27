package dev.foss.goldenpath.inventory

import android.content.Context
import android.net.Uri
import android.util.Log
import dev.foss.goldenpath.index.forge.FileGithubVerifiedStore
import dev.foss.goldenpath.index.forge.FilePastedRepoStore
import dev.foss.goldenpath.index.forge.FileWatchedRepoStore
import dev.foss.goldenpath.index.forge.ObtainiumImport
import java.io.File

object ObtainiumImportLive {
    fun readUri(context: Context, uri: Uri): String? =
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { ObtainiumImport.readUtf8(it) }
        }.getOrNull()

    fun applyJson(context: Context, json: String): ObtainiumImport.Result? {
        val parsed = runCatching { ObtainiumImport.parse(json) }.getOrNull() ?: return null
        if (parsed.imported == 0 && parsed.skipped == 0) return null
        val files = context.filesDir
        ObtainiumImport.persist(
            parsed.rows,
            FilePastedRepoStore(File(files, "pasted_repos.tsv")),
            FileGithubVerifiedStore(File(files, "github_verified.tsv")),
            FileWatchedRepoStore(File(files, "github_watched.tsv")),
        )
        Log.i("DevPulse", "obtainium import ${parsed.imported} skipped ${parsed.skipped}")
        return parsed
    }
}
