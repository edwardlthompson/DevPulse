package dev.foss.goldenpath.ui.opportunity

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import dev.foss.goldenpath.R
import dev.foss.goldenpath.opportunity.CategoryGap
import dev.foss.goldenpath.opportunity.OpportunityExport
import java.io.File

object OpportunityShare {
    fun send(context: Context, titles: List<String>, gaps: List<CategoryGap>, json: Boolean) {
        runCatching {
            val name = if (json) "devpulse-opportunity.json" else "devpulse-opportunity.csv"
            val mime = if (json) "application/json" else "text/csv"
            val body = if (json) OpportunityExport.json(titles, gaps) else OpportunityExport.csv(titles, gaps)
            val file = File(context.cacheDir, name)
            file.writeText(body, Charsets.UTF_8)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, context.getString(R.string.opportunity_export)))
        }
    }
}
