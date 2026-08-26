package dev.foss.goldenpath.index.forge

data class GithubAppOpt(
    val includePrereleases: Boolean = true,
    val apkRegex: String? = null,
)

object GithubAppOptCodec {
    const val MAX_REGEX = 64

    fun regexOrNull(raw: String?): Regex? {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty() || text.length > MAX_REGEX) return null
        return runCatching { Regex(text) }.getOrNull()
    }

    fun filename(url: String): String = url.substringAfterLast('/').substringBefore('?')
}
