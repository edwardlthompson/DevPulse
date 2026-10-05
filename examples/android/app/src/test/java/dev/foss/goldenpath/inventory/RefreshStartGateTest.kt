package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RefreshStartGateTest {
    @Test
    fun decidePrefersRunningThenWifi() {
        assertEquals(
            RefreshStartResult.AlreadyRunning,
            RefreshStartGate.decide(running = true, wifiOnly = true, unmetered = false),
        )
        assertEquals(
            RefreshStartResult.WifiBlocked,
            RefreshStartGate.decide(running = false, wifiOnly = true, unmetered = false),
        )
        assertEquals(
            RefreshStartResult.Started,
            RefreshStartGate.decide(running = false, wifiOnly = true, unmetered = true),
        )
    }

    @Test
    fun snackOnlyForBlockedPaths() {
        assertNull(RefreshStartCopy.snackRes(RefreshStartResult.Started))
        assertEquals(
            R.string.refresh_already_running,
            RefreshStartCopy.snackRes(RefreshStartResult.AlreadyRunning),
        )
        assertEquals(
            R.string.refresh_wifi_only,
            RefreshStartCopy.snackRes(RefreshStartResult.WifiBlocked),
        )
    }
}
