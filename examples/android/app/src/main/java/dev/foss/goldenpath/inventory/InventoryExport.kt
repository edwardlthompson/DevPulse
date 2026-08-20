package dev.foss.goldenpath.inventory

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class InventoryExportFormat(val fileName: String, val mimeType: String) {
    Html("devpulse-inventory.html", "text/html"),
    Csv("devpulse-inventory.csv", "text/csv"),
    Xml("devpulse-inventory.xml", "application/xml"),
}

object InventoryExport {
    fun render(apps: List<InstalledApp>, format: InventoryExportFormat): String = when (format) {
        InventoryExportFormat.Html -> InventoryExportMarkup.html(apps)
        InventoryExportFormat.Csv -> csv(apps)
        InventoryExportFormat.Xml -> InventoryExportMarkup.xml(apps)
    }

    fun listingUrl(link: UpdateLink): String? {
        if (!link.listed) return null
        val url = link.url?.trim().orEmpty()
        return url.takeIf { it.startsWith("http://") || it.startsWith("https://") }
    }

    internal fun isoDate(ms: Long?): String {
        if (ms == null) return ""
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date(ms))
    }

    internal fun escapeCsv(value: String): String {
        if (value.none { it == ',' || it == '"' || it == '\n' || it == '\r' }) return value
        return "\"${value.replace("\"", "\"\"")}\""
    }

    private fun csv(apps: List<InstalledApp>): String {
        val header = listOf(
            "label", "package", "installed_version", "installed_code",
            "last_release", "last_release_source", "remote_version", "remote_version_source",
            "listing_source", "listing_listed", "listing_known", "listing_version",
            "listing_date", "listing_url",
        ).joinToString(",")
        val rows = apps.flatMap { app ->
            val listings = app.latestListings.ifEmpty { listOf(null) }
            listings.map { link -> csvRow(app, link) }
        }
        return (listOf(header) + rows).joinToString("\n") + "\n"
    }

    private fun csvRow(app: InstalledApp, link: UpdateLink?): String {
        val cells = listOf(
            app.label,
            app.packageName,
            app.versionName.orEmpty(),
            app.versionCode.toString(),
            isoDate(app.remoteReleasedAtMs),
            app.remoteReleasedSource.name,
            app.remoteVersionName.orEmpty(),
            app.remoteVersionSource.name,
            link?.source?.name.orEmpty(),
            link?.listed?.toString().orEmpty(),
            link?.known?.toString().orEmpty(),
            link?.versionName.orEmpty(),
            isoDate(link?.releasedAtMs),
            link?.let(::listingUrl).orEmpty(),
        )
        return cells.joinToString(",") { escapeCsv(it) }
    }
}
