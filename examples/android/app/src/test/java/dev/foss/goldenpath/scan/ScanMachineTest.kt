package dev.foss.goldenpath.scan

import dev.foss.goldenpath.inventory.AppOrigin
import dev.foss.goldenpath.inventory.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanMachineTest {
    @Test
    fun pauseResumeAndComplete() {
        var progress = ScanMachine.start(2)
        assertEquals(ScanPhase.Running, progress.phase)
        progress = ScanMachine.pause(progress)
        assertEquals(ScanPhase.Paused, progress.phase)
        assertEquals(0, progress.completed)
        progress = ScanMachine.resume(progress)
        progress = ScanMachine.advance(progress)
        assertEquals(ScanPhase.Running, progress.phase)
        assertEquals(1, progress.completed)
        progress = ScanMachine.advance(progress)
        assertEquals(ScanPhase.Completed, progress.phase)
        assertEquals(2, progress.completed)
    }

    @Test
    fun localScanLeavesRemotesUnknown() {
        val app = InstalledApp(
            packageName = "app.user",
            label = "User",
            versionName = "1.0",
            versionCode = 1L,
            lastUpdateTimeMs = 99L,
            firstInstallTimeMs = 1L,
            minSdk = 26,
            targetSdk = 37,
            isSystemApp = false,
            origin = AppOrigin.Unknown,
        )
        val items = LocalScan.run(listOf(app), nowMs = 1_000L)
        assertEquals(1, items.size)
        assertFalse(items[0].repoFound)
        assertEquals(dev.foss.goldenpath.staleness.Badge.Unknown, items[0].staleness.badge)
        assertEquals(0L, items[0].staleness.installedLastUpdateMs)
        assertTrue(items[0].staleness.newestRemoteActivityMs == null)
    }
}
