package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateAllTallyTest {
    @Test
    fun splitsDownloadAndInstallSuccessFail() {
        val snaps = listOf(
            snap("wait", UpdateAllPhase.Wait),
            snap("fetch", UpdateAllPhase.Fetch, received = 12_000_000, expected = -1),
            snap("apply", UpdateAllPhase.Apply),
            snap("ok", UpdateAllPhase.Ok),
            snap("failDl", UpdateAllPhase.Fail, failDownload = true),
            snap("failIns", UpdateAllPhase.Fail, failDownload = false),
        )
        val counts = UpdateAllTally.of(snaps)
        assertEquals(6, counts.total)
        assertEquals(3, counts.downloadedOk)
        assertEquals(1, counts.downloadedFail)
        assertEquals(1, counts.installedOk)
        assertEquals(1, counts.installedFail)
        assertEquals(UpdateAllSegment(3, 1, 2), counts.downloadBar())
        assertEquals(UpdateAllSegment(1, 1, 1), counts.installBar())
    }

    @Test
    fun fetchBytesDoNotFillTheDownloadBar() {
        val snaps = List(20) { snap("ok$it", UpdateAllPhase.Ok) } +
            listOf(
                snap("dlA", UpdateAllPhase.Fetch, received = 99, expected = 100),
                snap("dlB", UpdateAllPhase.Fetch, received = 100, expected = 100),
                snap("waitA", UpdateAllPhase.Wait),
                snap("waitB", UpdateAllPhase.Wait),
            )
        val bar = UpdateAllTally.of(snaps).downloadBar()
        assertEquals(24, bar.total)
        assertEquals(20, bar.ok)
        assertEquals(0, bar.fail)
        assertEquals(4, bar.pending)
    }

    @Test
    fun installBarUsesDownloadedAppsNotTheWholeQueue() {
        val snaps = listOf(
            snap("ok", UpdateAllPhase.Ok),
            snap("ready", UpdateAllPhase.Ready),
            snap("failDl", UpdateAllPhase.Fail, failDownload = true),
            snap("wait", UpdateAllPhase.Wait),
        )
        val counts = UpdateAllTally.of(snaps)
        assertEquals(UpdateAllSegment(2, 1, 1), counts.downloadBar())
        assertEquals(UpdateAllSegment(1, 0, 1), counts.installBar())
    }

    @Test
    fun readyCountsAsDownloadedBeforeInstall() {
        val counts = UpdateAllTally.of(listOf(snap("a", UpdateAllPhase.Ready)))
        assertEquals(1, counts.downloadedOk)
        assertEquals(0, counts.downloadedFail)
        assertEquals(0, counts.installedOk)
        assertEquals(0, counts.installedFail)
        assertEquals(UpdateAllSegment(0, 0, 1), counts.installBar())
    }

    @Test
    fun rankedPutsActiveDownloadsFirst() {
        val snaps = listOf(
            snap("ok", UpdateAllPhase.Ok),
            snap("wait", UpdateAllPhase.Wait),
            snap("fetch", UpdateAllPhase.Fetch),
        )
        assertEquals(
            listOf("fetch", "wait", "ok"),
            UpdateAllTally.ranked(snaps).map { it.packageName },
        )
    }

    private fun snap(
        pkg: String,
        phase: UpdateAllPhase,
        received: Long = 0,
        expected: Long = -1,
        failDownload: Boolean = false,
    ) = UpdateAllSnap(pkg, pkg, RemoteReleasedSource.Play, phase, received, expected, failDownload)
}
