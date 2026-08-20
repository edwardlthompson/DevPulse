package dev.foss.goldenpath.inventory

import java.net.URI

object ApkDownloadUrl {
    fun httpsFile(raw: String?): String? {
        val text = raw?.trim().orEmpty()
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

    private fun allowed(host: String, url: String): Boolean {
        if (url.contains(".apk", ignoreCase = true)) return true
        return host.contains("apkpure") ||
            host.contains("aptoide") ||
            host.contains("f-droid") ||
            host.contains("izzysoft") ||
            host.contains("guardianproject") ||
            host.contains("calyxos") ||
            host.contains("github") ||
            host.contains("githubusercontent")
    }
}
