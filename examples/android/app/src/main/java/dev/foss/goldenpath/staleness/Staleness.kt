package dev.foss.goldenpath.staleness

object Staleness {
    const val STUB_TARGET_SDK = 37
    const val GREEN_MAX_EXCLUSIVE = 180
    const val AMBER_MAX_INCLUSIVE = 365
    const val MS_PER_DAY = 86_400_000L

    fun badgeForDays(days: Int): Badge = when {
        days < GREEN_MAX_EXCLUSIVE -> Badge.Green
        days <= AMBER_MAX_INCLUSIVE -> Badge.Amber
        else -> Badge.Red
    }

    fun compatibilityWarning(targetSdk: Int, stubTargetSdk: Int = STUB_TARGET_SDK): Boolean =
        stubTargetSdk - targetSdk > 3

    fun evaluate(input: StalenessInput, nowMs: Long): StalenessResult {
        val dated = input.remotes.filter { signal ->
            signal.countsAsActivity &&
                signal.lookup == RemoteLookup.SuccessDated &&
                signal.activityAtMs != null
        }
        val newest = dated.maxOfOrNull { it.activityAtMs!! }
        val days = newest?.let { ((nowMs - it) / MS_PER_DAY).toInt() }
        val failed = input.remotes.any { it.lookup == RemoteLookup.Failed }
        val successfulMissing = input.remotes.any { it.lookup == RemoteLookup.SuccessMissing }
        val anyChecked = input.remotes.any { it.lookup != RemoteLookup.NotChecked }
        val badge = when {
            days != null -> badgeForDays(days)
            !anyChecked -> Badge.Unknown
            failed -> Badge.Unknown
            successfulMissing -> Badge.Red
            else -> Badge.Unknown
        }
        return StalenessResult(
            newestRemoteActivityMs = newest,
            daysSinceActivity = days,
            badge = badge,
            installedLastUpdateMs = input.installedLastUpdateMs,
            compatibilityWarning = compatibilityWarning(input.targetSdk, input.stubTargetSdk),
        )
    }
}
