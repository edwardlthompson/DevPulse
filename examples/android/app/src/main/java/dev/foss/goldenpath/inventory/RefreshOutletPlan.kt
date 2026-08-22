package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidRepo

object RefreshOutletPlan {
    fun seed(
        clock: RefreshProgressClock,
        apps: Int,
        repos: List<FdroidRepo>,
        playOn: Boolean,
        aptoide: Boolean,
        forge: Boolean,
        leftover: Boolean,
        mirror: Boolean,
        pure: Boolean,
    ) {
        if (playOn) RefreshOutletBoard.plan(RefreshOutletIds.PLAY, "Play", apps)
        if (aptoide) RefreshOutletBoard.plan(RefreshOutletIds.APTOIDE, "Aptoide", apps)
        if (forge) RefreshOutletBoard.plan(RefreshOutletIds.GITHUB, "GitHub", apps)
        repos.forEach { RefreshOutletBoard.plan(RefreshOutletIds.fdroid(it.id), it.id, apps) }
        if (mirror) RefreshOutletBoard.plan(RefreshOutletIds.MIRROR, "ApkMirror", apps)
        if (pure) RefreshOutletBoard.plan(RefreshOutletIds.PURE, "ApkPure", apps)
        if (leftover) RefreshOutletBoard.plan(RefreshOutletIds.LEFTOVER, "GitLab", apps)
        clock.pulse()
    }
}
