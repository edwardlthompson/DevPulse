package dev.foss.goldenpath.inventory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import dev.foss.goldenpath.index.forge.ObtainiumImport
import java.io.File

object ObtainiumImportLaunch {
    const val EXTRA = "obtainium_import"

    fun maybeStart(context: Context, intent: Intent?) {
        if (intent == null) return
        val extra = intent.getStringExtra(EXTRA)?.let { ObtainiumImport.sandboxName(it) }
        intent.removeExtra(EXTRA)
        val fromFile = extra?.let { name ->
            listOfNotNull(context.filesDir, context.getExternalFilesDir(null))
                .map { File(it, name) }
                .firstOrNull { it.isFile }
                ?.readText(Charsets.UTF_8)
        }
        val json = fromFile ?: readStream(context, streamUri(intent)) ?: return
        ObtainiumImportLive.applyJson(context, json)
        if (fromFile == null) intent.data = null
    }

    private fun streamUri(intent: Intent): Uri? {
        intent.data?.let { return it }
        return if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
    }

    private fun readStream(context: Context, uri: Uri?): String? =
        uri?.let { ObtainiumImportLive.readUri(context, it) }
}
