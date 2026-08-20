package dev.foss.goldenpath.ui.inventory

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InventoryExport
import dev.foss.goldenpath.inventory.InventoryExportFormat
import java.io.File

object InventoryShare {
    fun send(context: Context, apps: List<InstalledApp>, format: InventoryExportFormat) {
        runCatching {
            val snapshot = apps.toList()
            val file = File(context.cacheDir, format.fileName)
            file.writeText(InventoryExport.render(snapshot, format), Charsets.UTF_8)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = format.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, context.getString(R.string.inventory_export)))
        }
    }
}
