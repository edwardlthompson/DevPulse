package dev.foss.goldenpath.index.fdroid

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class FdroidPageMeta(
    val lastUpdatedMs: Long?,
    val sourceCode: String?,
    val category: String? = null,
    val relatedPackages: List<String> = emptyList(),
    val apkName: String? = null,
)

object FdroidPackagePage {
    private val addedOn = Regex("""Added on ([A-Za-z]{3} \d{1,2}, \d{4})""")
    private val sourceHref = Regex(
        """href="(https://(?:github\.com|gitlab\.com|codeberg\.org)/[^"]+)"""",
        RegexOption.IGNORE_CASE,
    )
    private val categoryHref = Regex("""/categories/([^"'/?#]+)""", RegexOption.IGNORE_CASE)
    private val packageHref = Regex("""/packages/([A-Za-z0-9._]+)""")
    private val apkHref = Regex("""href="([^"]+\.apk)"""", RegexOption.IGNORE_CASE)

    fun parse(html: String, selfPackage: String? = null): FdroidPageMeta {
        val body = html.trim()
        if (body.isEmpty()) return FdroidPageMeta(null, null)
        val last = addedOn.findAll(body).map { monthDayYear(it.groupValues[1]) }.firstOrNull { it != null }
        val source = sourceHref.find(body)?.groupValues?.get(1)?.substringBefore('"')?.ifEmpty { null }
        val category = categoryHref.find(body)?.groupValues?.get(1)?.replace('+', ' ')?.replace('_', ' ')
            ?.trim()?.ifEmpty { null }
        val self = selfPackage?.trim().orEmpty()
        val related = packageHref.findAll(body).map { it.groupValues[1] }
            .filter { it.isNotEmpty() && it != self }
            .distinct()
            .take(12)
            .toList()
        return FdroidPageMeta(last, source, category, related, apkName(body, self))
    }

    internal fun apkName(html: String, selfPackage: String = ""): String? {
        val names = apkHref.findAll(html).map { it.groupValues[1].substringAfterLast('/').substringBefore('?') }
            .map { it.trim() }
            .filter { name ->
                name.endsWith(".apk", ignoreCase = true) &&
                    '/' !in name && '\\' !in name && ".." !in name
            }
            .distinct()
            .toList()
        if (names.isEmpty()) return null
        val self = selfPackage.trim()
        return names.firstOrNull { self.isNotEmpty() && it.startsWith(self) } ?: names.first()
    }

    internal fun monthDayYear(raw: String): Long? {
        val parser = SimpleDateFormat("MMM d, yyyy", Locale.US)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        return runCatching { parser.parse(raw.trim())?.time }.getOrNull()
    }
}
