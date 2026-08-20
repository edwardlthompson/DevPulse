package dev.foss.goldenpath.inventory

enum class InstalledDateSource {
    LastUpdate,
    FirstInstall,
    ApkMtime,
    Unknown,
}

data class InstalledDate(
    val ms: Long?,
    val source: InstalledDateSource,
)

object InstalledDateResolver {
    const val ANDROID_PUBLIC_MS = 1_222_128_000_000L
    const val FUTURE_SLACK_MS = 86_400_000L

    fun isPlausible(ms: Long, nowMs: Long): Boolean =
        ms > 0L && ms >= ANDROID_PUBLIC_MS && ms <= nowMs + FUTURE_SLACK_MS

    fun resolve(
        lastUpdateTimeMs: Long,
        firstInstallTimeMs: Long,
        apkLastModifiedMs: Long,
        nowMs: Long,
    ): InstalledDate {
        val candidates = listOf(
            lastUpdateTimeMs to InstalledDateSource.LastUpdate,
            firstInstallTimeMs to InstalledDateSource.FirstInstall,
            apkLastModifiedMs to InstalledDateSource.ApkMtime,
        ).filter { (ms, _) -> isPlausible(ms, nowMs) }
        if (candidates.isEmpty()) {
            return InstalledDate(ms = null, source = InstalledDateSource.Unknown)
        }
        val best = candidates.maxBy { it.first }
        return InstalledDate(ms = best.first, source = best.second)
    }
}
