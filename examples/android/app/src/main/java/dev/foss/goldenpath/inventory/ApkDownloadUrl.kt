package dev.foss.goldenpath.inventory

import java.net.URI

object ApkDownloadUrl {
    fun httpsFile(raw: String?): String? {
        val text = jsonUrl(raw?.trim().orEmpty())
        if (text.isEmpty()) return null
        val https = text.replaceFirst(Regex("^http://", RegexOption.IGNORE_CASE), "https://")
        if (!https.startsWith("https://", ignoreCase = true)) return null
        if (https.contains("/XAPK", ignoreCase = true)) return null
        return runCatching {
            val uri = URI(https)
            val host = uri.host.orEmpty().lowercase()
            if (host.isEmpty() || !allowed(host, https)) null else https
        }.getOrNull()
    }

    fun jsonUrl(text: String): String =
        text.replace("\\u0026", "&", ignoreCase = true).replace("\\/", "/")

    private fun allowed(host: String, url: String): Boolean {
        if (!publicHost(host)) return false
        if (url.contains(".apk", ignoreCase = true)) return true
        return host.contains("apkpure") ||
            host.contains("cdnpure") ||
            host.contains("pureapk") ||
            host.contains("apkmirror") ||
            host.contains("aptoide") ||
            host.contains("f-droid") ||
            host.contains("izzysoft") ||
            host.contains("guardianproject") ||
            host.contains("calyxos") ||
            host.contains("microg.org") ||
            host.contains("newpipe.net") ||
            host.contains("divestos.org") ||
            host.contains("kde.org") ||
            host.contains("cromite.org") ||
            host.contains("iode.tech") ||
            host.contains("github") ||
            host.contains("githubusercontent") ||
            host.endsWith("gvt1.com") ||
            host.endsWith("googleusercontent.com") ||
            host.endsWith("play.googleapis.com") ||
            host.endsWith("android.clients.google.com")
    }

    private fun publicHost(host: String): Boolean {
        if (host.isEmpty() || host == "localhost" || host.endsWith(".local")) return false
        if (host.all { it.isDigit() || it == '.' || it == ':' }) return false
        return true
    }
}
