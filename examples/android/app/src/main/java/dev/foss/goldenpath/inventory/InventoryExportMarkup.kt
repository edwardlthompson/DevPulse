package dev.foss.goldenpath.inventory

object InventoryExportMarkup {
    fun html(apps: List<InstalledApp>): String {
        val rows = apps.joinToString("") { app ->
            val listings = app.latestListings.joinToString("") { link ->
                val href = InventoryExport.listingUrl(link)
                val url = if (href != null) {
                    """<a href="${escape(href)}">${escape(href)}</a>"""
                } else {
                    ""
                }
                "<li>${escape(link.source.name)} listed=${link.listed} known=${link.known} " +
                    "version=${escape(link.versionName.orEmpty())} " +
                    "date=${escape(InventoryExport.isoDate(link.releasedAtMs))} $url</li>"
            }
            "<tr><td>${escape(app.label)}</td><td>${escape(app.packageName)}</td>" +
                "<td>${escape(app.versionName.orEmpty())}</td><td>${app.versionCode}</td>" +
                "<td>${escape(InventoryExport.isoDate(app.remoteReleasedAtMs))}</td>" +
                "<td>${escape(app.remoteReleasedSource.name)}</td>" +
                "<td>${escape(app.remoteVersionName.orEmpty())}</td>" +
                "<td>${escape(app.remoteVersionSource.name)}</td>" +
                "<td><ul>$listings</ul></td></tr>"
        }
        return """<!DOCTYPE html><html><head><meta charset="utf-8"><title>DevPulse</title></head><body>
<table><thead><tr><th>Label</th><th>Package</th><th>Installed version</th><th>Installed code</th><th>Last release</th><th>Last release source</th><th>Remote version</th><th>Remote version source</th><th>Listings</th></tr></thead><tbody>$rows</tbody></table>
</body></html>
"""
    }

    fun xml(apps: List<InstalledApp>): String {
        val body = apps.joinToString("") { app ->
            val listings = app.latestListings.joinToString("") { link ->
                val href = InventoryExport.listingUrl(link)
                val url = if (href != null) "<url>${escape(href)}</url>" else ""
                """<listing source="${escape(link.source.name)}" listed="${link.listed}" known="${link.known}"><version>${escape(link.versionName.orEmpty())}</version><date>${escape(InventoryExport.isoDate(link.releasedAtMs))}</date>$url</listing>"""
            }
            """<app><label>${escape(app.label)}</label><package>${escape(app.packageName)}</package><installedVersion>${escape(app.versionName.orEmpty())}</installedVersion><installedCode>${app.versionCode}</installedCode><lastRelease>${escape(InventoryExport.isoDate(app.remoteReleasedAtMs))}</lastRelease><lastReleaseSource>${escape(app.remoteReleasedSource.name)}</lastReleaseSource><remoteVersion>${escape(app.remoteVersionName.orEmpty())}</remoteVersion><remoteVersionSource>${escape(app.remoteVersionSource.name)}</remoteVersionSource><listings>$listings</listings></app>"""
        }
        return """<?xml version="1.0" encoding="UTF-8"?><inventory>$body</inventory>"""
    }

    internal fun escape(value: String): String =
        value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}
