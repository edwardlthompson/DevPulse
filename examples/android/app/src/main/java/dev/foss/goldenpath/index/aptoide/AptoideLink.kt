package dev.foss.goldenpath.index.aptoide

/** Aptoide listing URLs. `{uname}.en.aptoide.com` is an app view; `en.aptoide.com` is home. */
object AptoideLink {
    const val STORE_PACKAGE = "cm.aptoide.pt"
    const val INSTALL_PAGE = "https://en.aptoide.com/download"
    const val GAMES_ACTIVITY = "aptoidegames"
    private val slug = Regex("^[A-Za-z0-9][A-Za-z0-9_-]{0,80}$")

    fun webPage(packageName: String, uname: String? = null): String {
        val slugName = uname?.trim()?.takeIf { slug.matches(it) }
        if (slugName != null) return "https://$slugName.en.aptoide.com/"
        return "https://en.aptoide.com/app?package_name=$packageName"
    }

    fun appOpenUri(listingUrl: String, packageName: String? = null): String {
        val pkg = packageName?.trim()?.takeIf { it.isNotEmpty() }
            ?: queryValue(listingUrl, "package_name")
        if (pkg != null) return "https://en.aptoide.com/app?package_name=$pkg"
        return listingUrl
    }

    fun isGamesClient(activityClass: String?): Boolean =
        activityClass?.contains(GAMES_ACTIVITY, ignoreCase = true) == true

    private fun queryValue(url: String, key: String): String? =
        url.substringAfter('?', "")
            .split('&')
            .firstOrNull { it.startsWith("$key=") }
            ?.substringAfter('=')
            ?.takeIf { it.isNotEmpty() }
}
