package dev.foss.goldenpath.inventory

/** Drop wasted fallbacks after a listing we already know cannot install. */
internal object UpdateAllSkip {
    private val playSideload = setOf(
        RemoteReleasedSource.ApkPure,
        RemoteReleasedSource.ApkMirror,
    )

    fun allowSideloadMirror(app: InstalledApp, source: RemoteReleasedSource): Boolean {
        if (source !in playSideload) return true
        val play = app.latestListings.firstOrNull {
            it.source == RemoteReleasedSource.Play && it.listed
        } ?: return true
        return VersionCompare.isNewer(
            play.versionName,
            app.versionName,
            app.versionCode,
            play.versionCode,
        )
    }

    fun dropSideloadAfterPlayOlder(
        group: MutableList<UpdateAllJob>,
        why: InstallWhy,
        source: RemoteReleasedSource,
    ) {
        if (why == InstallWhy.Older && source == RemoteReleasedSource.Play) {
            group.removeAll { it.source in playSideload }
        }
    }

    fun stopFallbacks(group: MutableList<UpdateAllJob>) {
        group.clear()
    }

    fun dlLine(ok: Boolean, why: InstallWhy): String = when {
        ok -> "ok"
        why == InstallWhy.Older || why == InstallWhy.Sdk -> "skip ${why.name}"
        else -> "fail ${why.name}"
    }
}
