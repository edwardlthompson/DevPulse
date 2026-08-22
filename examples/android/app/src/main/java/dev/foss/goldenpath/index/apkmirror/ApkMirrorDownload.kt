package dev.foss.goldenpath.index.apkmirror

object ApkMirrorDownload {
    private val file = Regex(
        """/wp-content/themes/APKMirror/download\.php\?id=\d+[^"'\s]*""",
        RegexOption.IGNORE_CASE,
    )
    private val button = Regex(
        """href=["'](/apk/[^"']+/download/?[^"']*)["']""",
        RegexOption.IGNORE_CASE,
    )
    private val variant = Regex(
        """href=["'](/apk/[^"']+-apk-download/)["']""",
        RegexOption.IGNORE_CASE,
    )

    fun fileUrl(html: String): String? = file.find(html)?.value?.let(::absolute)

    fun nextPage(html: String): String? {
        val rel = button.find(html)?.groupValues?.get(1) ?: variant.find(html)?.groupValues?.get(1)
        return rel?.replace("&amp;", "&")?.let(::absolute)
    }

    private fun absolute(path: String): String {
        val clean = path.trim()
        if (clean.startsWith("https://")) return clean
        return "https://www.apkmirror.com$clean"
    }
}
