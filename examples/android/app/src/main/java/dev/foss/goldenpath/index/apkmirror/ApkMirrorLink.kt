package dev.foss.goldenpath.index.apkmirror

object ApkMirrorLink {
    fun webPage(packageName: String, path: String? = null): String {
        val rel = path?.trim().orEmpty()
        if (rel.startsWith("/")) return "https://www.apkmirror.com$rel"
        return "https://www.apkmirror.com/?post_type=app_release&searchtype=apk&s=$packageName"
    }
}
