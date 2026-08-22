package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RefreshOutletPlanTest {
    @Before
    fun reset() {
        RefreshSkip.reset()
        RefreshOutletBoard.reset()
        RefreshPaceBook.clear()
    }

    @Test
    fun seedEmitsEveryEnabledSourceOnce() {
        val ticks = mutableListOf<RefreshProgress>()
        val clock = RefreshProgressClock { ticks.add(it) }
        val official = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
        RefreshOutletPlan.seed(
            clock, 10, listOf(official),
            playOn = true, aptoide = true, forge = true, leftover = false,
            mirror = true, pure = true,
        )
        assertEquals(1, ticks.size)
        assertEquals(
            listOf(
                RefreshOutletIds.PLAY,
                RefreshOutletIds.APTOIDE,
                RefreshOutletIds.GITHUB,
                RefreshOutletIds.fdroid("official"),
                RefreshOutletIds.MIRROR,
                RefreshOutletIds.PURE,
            ),
            ticks.single().outlets.map { it.id },
        )
    }
}
