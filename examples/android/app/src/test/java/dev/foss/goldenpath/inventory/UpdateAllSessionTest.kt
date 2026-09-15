package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateAllSessionTest {
    @Before
    fun reset() {
        UpdateAllSession.busy.value = false
        UpdateAllSession.snaps.value = emptyList()
        UpdateAllSession.visible.value = false
    }

    @Test
    fun seedWaitFillsIdleRows() {
        UpdateAllSession.seedWait(
            listOf(UpdateAllJob("app.a", "A", RemoteReleasedSource.Play, null, "2.0")),
        )
        val snap = UpdateAllSession.snaps.value.single()
        assertEquals("app.a", snap.packageName)
        assertEquals(UpdateAllPhase.Wait, snap.phase)
        assertTrue(snap.stay)
    }

    @Test
    fun seedWaitDoesNotClobberALiveRun() {
        UpdateAllSession.busy.value = true
        val live = UpdateAllSnap("app.b", "B", RemoteReleasedSource.Fdroid, UpdateAllPhase.Fetch)
        UpdateAllSession.snaps.value = listOf(live)
        UpdateAllSession.seedWait(
            listOf(UpdateAllJob("app.a", "A", RemoteReleasedSource.Play, null, "2.0")),
        )
        assertEquals(listOf("app.b"), UpdateAllSession.snaps.value.map { it.packageName })
        assertEquals(UpdateAllPhase.Fetch, UpdateAllSession.snaps.value.single().phase)
    }

    @Test
    fun seedWaitKeepsFinishedRows() {
        UpdateAllSession.snaps.value = listOf(
            UpdateAllSnap("app.a", "A", RemoteReleasedSource.Forge, UpdateAllPhase.Fail),
        )
        UpdateAllSession.seedWait(
            listOf(UpdateAllJob("app.a", "A", RemoteReleasedSource.Forge, null, "2.0")),
        )
        assertEquals(UpdateAllPhase.Fail, UpdateAllSession.snaps.value.single().phase)
    }
}
