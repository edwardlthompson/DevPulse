package dev.foss.goldenpath.staleness

enum class RemoteSource {
    Play,
    Fdroid,
    ExtraRepo,
    Forge,
}

enum class RemoteLookup {
    NotChecked,
    Failed,
    SuccessMissing,
    SuccessDated,
}

data class RemoteSignal(
    val source: RemoteSource,
    val lookup: RemoteLookup,
    val activityAtMs: Long? = null,
    val countsAsActivity: Boolean = lookup == RemoteLookup.SuccessDated && activityAtMs != null,
)

enum class Badge {
    Green,
    Amber,
    Red,
    Unknown,
}

data class StalenessInput(
    val remotes: List<RemoteSignal>,
    val installedLastUpdateMs: Long?,
    val targetSdk: Int,
    val stubTargetSdk: Int = Staleness.STUB_TARGET_SDK,
)

data class StalenessResult(
    val newestRemoteActivityMs: Long?,
    val daysSinceActivity: Int?,
    val badge: Badge,
    val installedLastUpdateMs: Long?,
    val compatibilityWarning: Boolean,
)
