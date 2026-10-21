package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class ReleaseRefreshWorkerGateTest {
    @Test
    fun skipsWhenCannotScan() {
        assertEquals(
            ReleaseRefreshWorkerOutcome.SkipSuccess,
            ReleaseRefreshWorkerGate.decide(canScan = false, wifiOnly = false, unmetered = true),
        )
    }

    @Test
    fun retriesWhenWifiOnlyOnMetered() {
        assertEquals(
            ReleaseRefreshWorkerOutcome.RetryLater,
            ReleaseRefreshWorkerGate.decide(canScan = true, wifiOnly = true, unmetered = false),
        )
    }

    @Test
    fun readyWhenAllowed() {
        assertEquals(
            ReleaseRefreshWorkerOutcome.Ready,
            ReleaseRefreshWorkerGate.decide(canScan = true, wifiOnly = true, unmetered = true),
        )
        assertEquals(
            ReleaseRefreshWorkerOutcome.Ready,
            ReleaseRefreshWorkerGate.decide(canScan = true, wifiOnly = false, unmetered = false),
        )
    }
}
