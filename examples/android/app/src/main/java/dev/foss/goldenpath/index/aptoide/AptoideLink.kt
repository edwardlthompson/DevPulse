package dev.foss.goldenpath.index.aptoide

/** Aptoide listing URLs. `{uname}.en.aptoide.com` is an app view; `en.aptoide.com` is home. */
object AptoideLink {
    const val STORE_PACKAGE = "cm.aptoide.pt"
    private val slug = Regex("^[A-Za-z0-9][A-Za-z0-9_-]{0,80}$")

    fun webPage(packageName: String, uname: String? = null): String {
        val slugName = uname?.trim()?.takeIf { slug.matches(it) }
        if (slugName != null) return "https://$slugName.en.aptoide.com/"
        return "https://en.aptoide.com/app?package_name=$packageName"
    }

    fun appOpenUri(listingUrl: String): String {
        val host = hostOf(listingUrl) ?: return listingUrl
        if (host.endsWith(".aptoide.com") && host.split('.').size >= 4) return listingUrl
        val pkg = queryValue(listingUrl, "package_name") ?: return listingUrl
        return "aptoidesearch://$pkg"
    }

    private fun hostOf(url: String): String? {
        val after = url.substringAfter("://", missingDelimiterValue = "")
        if (after.isEmpty()) return null
        return after.substringBefore('/').substringBefore('?').lowercase()
    }

    private fun queryValue(url: String, key: String): String? =
        url.substringAfter('?', "")
            .split('&')
            .firstOrNull { it.startsWith("$key=") }
            ?.substringAfter('=')
            ?.takeIf { it.isNotEmpty() }
}
