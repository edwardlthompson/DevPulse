package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateAllStayTest {
    @Before
    fun reset() {
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
    }

    @Test
    fun successHidesAppAndLastFailHidesWhenNoRetry() {
        val snaps = mutableListOf<UpdateAllSnap>()
        UpdateAll.run(
            jobs = listOf(UpdateAllJob("app.ok", "Ok", RemoteReleasedSource.Play, null, "2.0")),
            prepare = { _, _ -> listOf(File.createTempFile("okapk", ".apk")) },
            install = { true },
            onSnap = { snaps += it },
        )
        assertFalse(snaps.last { it.phase == UpdateAllPhase.Ok }.stay)
        assertTrue(AppliedUpdates.settled("app.ok"))
        snaps.clear()
        UpdateAll.run(
            jobs = listOf(UpdateAllJob("app.no", "No", RemoteReleasedSource.Play, null, "2.0")),
            prepare = { _, _ -> null },
            install = { false },
            onSnap = { snaps += it },
        )
        assertFalse(snaps.last { it.phase == UpdateAllPhase.Fail }.stay)
        assertTrue(IgnoredUpdates.has("app.no", RemoteReleasedSource.Play, "2.0"))
    }

    @Test
    fun failedNewestKeepsRowUntilRetrySucceeds() {
        val play = UpdateAllJob("app.a", "A", RemoteReleasedSource.Play, null, "3.0")
        val fdroid = UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null, "2.0")
        val snaps = mutableListOf<UpdateAllSnap>()
        UpdateAll.run(
            jobs = listOf(play),
            groups = listOf(listOf(play, fdroid)),
            prepare = { job, _ ->
                if (job.source == RemoteReleasedSource.Play) null else listOf(File.createTempFile("okapk", ".apk"))
            },
            install = { true },
            onSnap = { snaps += it },
        )
        assertTrue(snaps.first { it.phase == UpdateAllPhase.Fail }.stay)
        assertFalse(snaps.last { it.phase == UpdateAllPhase.Ok }.stay)
    }
}
