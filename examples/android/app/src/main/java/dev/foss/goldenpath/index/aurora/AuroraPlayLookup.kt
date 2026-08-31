package dev.foss.goldenpath.index.aurora

import dev.foss.goldenpath.inventory.InstalledDateResolver
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

enum class AuroraPlayStatus {
    Listed,
    Missing,
    Unknown,
}

data class AuroraPlayApp(
    val status: AuroraPlayStatus,
    val versionName: String? = null,
    val updatedOnMs: Long? = null,
    val versionCode: Long? = null,
)

fun interface AuroraPlayDetails {
    fun getMany(packageNames: List<String>): Map<String, AuroraPlayApp>
}

object AuroraPlayLookup {
    const val CHUNK = 20

    fun parseUpdatedOn(raw: String?, nowMs: Long): Long? {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty() || text.contains("ago", ignoreCase = true)) return null
        val ms = listOf("MMM d, yyyy", "MMMM d, yyyy").firstNotNullOfOrNull { pattern ->
            val parser = SimpleDateFormat(pattern, Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            parser.isLenient = false
            runCatching { parser.parse(text)?.time }.getOrNull()
        } ?: return null
        return ms.takeIf { InstalledDateResolver.isPlausible(it, nowMs) }
    }

    fun fromFields(versionName: String?, versionCode: Long, updatedOn: String?, nowMs: Long): AuroraPlayApp {
        val version = versionName?.trim()?.ifEmpty { null }
        if (version == null && versionCode <= 0 && updatedOn.isNullOrBlank()) {
            return AuroraPlayApp(AuroraPlayStatus.Missing)
        }
        return AuroraPlayApp(
            status = AuroraPlayStatus.Listed,
            versionName = version,
            updatedOnMs = parseUpdatedOn(updatedOn, nowMs),
            versionCode = versionCode.takeIf { it > 0 },
        )
    }

    fun fillOmitted(
        wanted: List<String>,
        found: Map<String, AuroraPlayApp>,
        retry: (List<String>) -> Map<String, AuroraPlayApp>,
    ): Map<String, AuroraPlayApp> {
        val holes = wanted.filter {
            val status = found[it]?.status
            status == null || status == AuroraPlayStatus.Missing
        }
        val extra = if (holes.isEmpty()) emptyMap() else retry(holes)
        return wanted.associateWith { pkg ->
            val have = found[pkg]
            when (have?.status) {
                AuroraPlayStatus.Listed, AuroraPlayStatus.Unknown -> have
                else -> extra[pkg] ?: have ?: AuroraPlayApp(AuroraPlayStatus.Missing)
            }
        }
    }
}
