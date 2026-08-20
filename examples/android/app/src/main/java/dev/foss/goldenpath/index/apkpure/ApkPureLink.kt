package dev.foss.goldenpath.index.apkpure

object ApkPureLink {
    const val STORE_PACKAGE = "com.apkpure.aegon"

    fun webPage(packageName: String): String = "https://apkpure.com/search?q=$packageName"

    fun appOpenUri(listingUrl: String, packageName: String? = null): String {
        val pkg = packageName?.trim()?.takeIf { it.isNotEmpty() }
        if (pkg != null) return "market://details?id=$pkg"
        return listingUrl
    }
}
