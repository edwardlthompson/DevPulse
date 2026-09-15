package dev.foss.goldenpath.inventory

object UpdateAllFollow {
    fun magnet(userScrolled: Boolean): Boolean = !userScrolled
}

object ScanUpdateCta {
    fun visible(complete: Boolean, count: Int): Boolean = autoStart(complete, count)

    fun autoStart(complete: Boolean, count: Int): Boolean = complete && count > 0

    fun inFlight(snaps: List<UpdateAllSnap>): Boolean =
        snaps.any {
            it.phase != UpdateAllPhase.Ok && it.phase != UpdateAllPhase.Fail
        }

    fun hideWhenIdle(
        lookupDone: Boolean,
        refreshing: Boolean,
        busy: Boolean,
        snaps: List<UpdateAllSnap>,
    ): Boolean = lookupDone && !refreshing && !busy && !inFlight(snaps)
}
